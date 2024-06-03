package Vehicles;

import State.GlobalState;
import State.Navigator;
import Tiles.RoadTile;
import Tiles.Tile;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import java.util.List;

public class CarAgent extends Agent {
    private int[] location = new int[]{0, 2};   // TODO: make this random
    private int id;
    private GlobalState globalState;

    public int getId(){
        return id;
    }

    private class RoamBehaviour extends Behaviour {
        private GlobalState globalState;
        private CarAgent myCarAgent;

        private Navigator navigator;
        private int newX, newY;
        private List<Tile> path;
        private int pathIdx;
        private boolean navDone;
        private boolean crashed = false;

        public boolean getCrashed(){
            return crashed;
        }

        public void setCrashed(boolean val){
            crashed = val;
        }

        // TODO: this is not a roam but a goTo
        // TODO: pick random point (check if valid)
        public RoamBehaviour(Agent a, CarAgent agent, int newX, int newY){
            super(a);

            // init variables
            this.globalState = GlobalState.getInstance();
            this.myAgent = agent;
            navigator = new Navigator(globalState.getGrid());
            navDone = false;
            this.newX = newX;
            this.newY = newY;

            // get path
            int x = location[0];
            int y = location[1];
            Tile[][] grid = globalState.getGrid();
            RoadTile start = (RoadTile) grid[y][x];
            RoadTile end = (RoadTile) grid[newY][newX];
            path = navigator.findPath(start, end);
            pathIdx = 1;
            globalState.updatePathDisplay(path);
        }

        @Override
        public void action() {
            int x = location[0];
            int y = location[1];

            if (pathIdx > path.size()) {
                navDone = true;
                return;
            }

            // NOTE: here x and y are columns and rows correspondingly (i.e. they are inverted)
            int xPath = path.get(pathIdx).getY();
            int yPath = path.get(pathIdx).getX();

            int moveState = globalState.moveCar(x, y, xPath, yPath, myCarAgent);
            switch(moveState){
                case 1:
                    location[0] = xPath;
                    location[1] = yPath;
                    break;
                case 2:
                    crashed = true;
                    System.out.printf("[Car%d] crashed!", myCarAgent.getId());
                    return;
                default:
                    break;
            }

            // check if done
            if(location[0] == newX && location[1] == newY){
                navDone = true;
                return;
            }
            pathIdx++;
            myAgent.doWait(1000);
        }

        @Override
        public boolean done() {
            return navDone || crashed;
        }
    }

    public void setup(){
        globalState = GlobalState.getInstance();
        Object[] args = getArguments();

        if(args != null){
            id = Integer.parseInt(args[0].toString());
            location[0] = Integer.parseInt(args[1].toString());
            location[1] = Integer.parseInt(args[2].toString());
        }
        System.out.println("[Vehicles.CarAgent] Accessed Grid with id: " + id);

        globalState.setCarLocation(location[0], location[1], this);
        addBehaviour(new RoamBehaviour(this, this, 2, 6));
    }
}

