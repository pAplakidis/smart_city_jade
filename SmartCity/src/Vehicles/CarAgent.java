package Vehicles;

import State.GlobalState;
import State.Navigator;
import Tiles.RoadTile;
import Tiles.Tile;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.xml.xpath.XPath;
import java.util.List;

public class CarAgent extends Agent {
    private int[] location = new int[]{2, 0};   // TODO: make this random
    private int id = 1;
    private GlobalState globalState;
    private boolean crashed = false;
    private int lookahead = 2;  // 0: naive, 1: greedy, 2: cooperative

    public boolean getCrashed() {
        return crashed;
    }

    public void setCrashed(boolean val) {
        crashed = val;
        if (crashed) {
            System.out.println("[" + getLocalName() + "] Crush at (" + location[1] + ", " + location[0] + ")");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new jade.core.AID("Hosp", jade.core.AID.ISLOCALNAME));
            msg.setContent("Crush at location " + location[1] + ", " + location[0]);
            send(msg);
        }
    }

    public int getId() {
        return id;
    }

    private class RoamBehaviour extends Behaviour {
        private GlobalState globalState;
        private CarAgent myCarAgent;

        Tile[][] grid;
        private Navigator navigator;
        private int newX, newY;
        private List<Tile> path;
        private int pathIdx;
        private boolean navDone;

        // TODO: this is not a roam but a goTo
        // TODO: pick random point (check if valid)
        public RoamBehaviour(Agent a, CarAgent agent, int newX, int newY) {
            super(a);
            System.out.println("[agent] " + myAgent.getAID().getLocalName());
            // init variables
            this.globalState = GlobalState.getInstance();
            this.myCarAgent = agent;
            navigator = new Navigator(globalState.getGrid());
            navDone = false;
            this.newX = newX;
            this.newY = newY;

            // get path
            int x = location[0];
            int y = location[1];
            grid = globalState.getGrid();
            RoadTile start = (RoadTile) grid[y][x];
            RoadTile end = (RoadTile) grid[newY][newX];
            path = navigator.findPath(start, end);
            pathIdx = 1;
            globalState.updatePathDisplay(path);
        }

        private boolean hasToStopAtIntersection(int y, int x, boolean hasPriority) {
            // check for intersection and handle priorities
            grid = globalState.getGrid();

            if (lookahead == 0 || grid[y][x].getValue() != -1) {
                return false;
            }

            // TODO: what if both cars have priority (or both don't)?
//            System.out.printf("Prio: %b - Car Detected\n", hasPriority, globalState.checkCarsInIntersection(x, y, id, location[0], location[1], lookahead));
            if (hasPriority || !globalState.checkCarsInIntersection(x, y, id, location[0], location[1], lookahead)) {
                return false;
            }

//            System.out.printf("[Car %d]: waiting for priority\n", id);
            return true;
        }

        @Override
        public void action() {
            if (!navDone) {
                // Follow path from Navigator
                int x = location[0];
                int y = location[1];

//                System.out.printf("%d/%d\n", pathIdx, path.size());
                if (pathIdx >= path.size()) {
                    navDone = true;
                    myAgent.doWait(1000);
                    return;
                }

                // TODO: cleanup => sub-behaviours can be functions
                // TODO: move this behaviour to a function and call it recursively if reached path, with random point
                // NOTE: here x and y are columns and rows correspondingly (i.e. they are inverted)
                boolean hasPriority = ((RoadTile) globalState.getGrid()[y][x]).hasPriority();
                int xPath = path.get(pathIdx).getY();
                int yPath = path.get(pathIdx).getX();

                // either wait or continue if near intersection and not on it
                if (grid[location[0]][location[1]].getValue() != -1 &&
                        hasToStopAtIntersection(xPath, yPath, hasPriority)) {
                    myAgent.doWait(1000);
                    return;
                }

                int moveState = globalState.moveCar(x, y, xPath, yPath, myCarAgent);
                switch (moveState) {
                    case 1:
                        location[0] = xPath;
                        location[1] = yPath;
                        break;
                    case 2:
                        myAgent.doWait(2000);
                        setCrashed(true);
                        System.out.printf("[Car%d] crashed!\n", myCarAgent.getId());
                        return;
                    case 3:
                        break;
                    default:
                        break;
                }

                // check if done
                if (location[0] == newX && location[1] == newY) {
                    navDone = true;
                    return;
                }
                pathIdx++;
                myAgent.doWait(1000);
//            myAgent.doWait(3000);
            }else{
                // Get new path from Navigator
                RoadTile start = (RoadTile) globalState.getGrid()[location[1]][location[0]];
                RoadTile end = navigator.getRandomRoadTile();
//                System.out.printf("New destination: %d,%d\n", end.getX(), end.getY());
                pathIdx = 1;
                path = navigator.findPath(start, end);
                navDone = false;
            }
        }

        @Override
        public boolean done() {
            return crashed;
        }
    }

    public void setup() {
        globalState = GlobalState.getInstance();
        Object[] args = getArguments();

        if (args != null) {
            id = Integer.parseInt(args[0].toString());
            location[0] = Integer.parseInt(args[1].toString());
            location[1] = Integer.parseInt(args[2].toString());
        }
//        System.out.println("[Vehicles.CarAgent] Accessed Grid with id: " + id);

        globalState.setCarLocation(location[0], location[1], this);
//        addBehaviour(new RoamBehaviour(this, this, 6, 8));
        addBehaviour(new RoamBehaviour(this, this, 3, 5));
    }
}

