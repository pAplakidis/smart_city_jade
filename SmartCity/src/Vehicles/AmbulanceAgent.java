package Vehicles;

import Tiles.BuildingTile;
import Tiles.RoadTile;
import Tiles.Tile;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import State.GlobalState;
import State.Navigator;

import java.util.List;

public class AmbulanceAgent extends CarAgent {
    private int id = 166;
    private int[] location = new int[]{24, 15};
    private int[] crushLocation = new int[]{0, 0};
    private int[] stationLocation = new int[]{22, 8};
    private int loadingProgress = 100;
    private boolean isAddressingCrush = false;
    private List<Tile> path = null;
    private GlobalState globalState;
    private Navigator navigator;
    private RoadTile currentLocation;
    private RoadTile nextLocation;

    private enum State {
        ROAMING,
        MOVING_TO_CRUSH,
        LOADING_INJURED,
        RETURNING_TO_HOSPITAL
    }

    private State currentState = State.ROAMING;

    public int getId() {
        return id;
    }

    public void setup() {
        globalState = GlobalState.getInstance();
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            id = Integer.parseInt(args[0].toString());
            location = new int[]{Integer.parseInt(args[1].toString()), Integer.parseInt(args[2].toString())};
        }

        System.out.println("[" + this.getLocalName() + "] Accessed Grid with id: " + id);
        globalState.setCarLocation(location[1], location[0], this);
        navigator = new Navigator(globalState.getGrid());

        currentLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];


        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mtRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                MessageTemplate mtAssign = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                ACLMessage msg = myAgent.receive(mtRequest);
                if (msg != null && msg.getContent().startsWith("Request distance for")) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    if (isAddressingCrush) {
                        reply.setContent("9999");
                        send(reply);
                    } else {
                        System.out.println("[" + getLocalName() + "] Received and Answering distance request from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        String locationString = msg.getContent().substring(30);
                        reply.setContent(String.valueOf(getDistanceToCrush(locationString)));
                        send(reply);
                    }
                } else {
                    msg = myAgent.receive(mtAssign);
                    if (msg != null) {
                        System.out.println("[" + getLocalName() + "] Addressing crush at location " + crushLocation[0] + ", " + crushLocation[1]);
                        isAddressingCrush = true;
                        nextLocation = null;
                        currentState = AmbulanceAgent.State.MOVING_TO_CRUSH;
                    } else {
                        block();
                    }
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 500) { // State management for moving, extinguishing, etc.
            protected void onTick() {
                switch (currentState) {
                    case ROAMING:
                        roam();
                        break;
                    case MOVING_TO_CRUSH:
                        moveToCrush();
                        break;
                    case LOADING_INJURED:
                        loadingInjured();
                        break;
                    case RETURNING_TO_HOSPITAL:
                        returnToHospital();
                        break;
                }
            }
        });
    }

    private void roam() {
        // Logic to roam around the city
        if (nextLocation == null || currentLocation == nextLocation) {
            do {
                nextLocation = navigator.getRandomRoadTile();
            } while (nextLocation == currentLocation && navigator.findPath(currentLocation, nextLocation).size() <= 10);
            path = navigator.findPath(currentLocation, nextLocation);
        }
//        System.out.println("[" + this.getLocalName() + "] Moving from " + currentLocation.getX() + " " + currentLocation.getY() + " to " + nextLocation.getX() + " " + nextLocation.getY() + " with path length " + path.size());

        int[] new_location = new int[2];
        new_location[0] = path.get(1).getX();
        new_location[1] = path.get(1).getY();

        globalState.moveCar(location[1], location[0], new_location[1], new_location[0], this);
        location = new_location;
        currentLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];

        path = path.subList(1, path.size());
        // show path after the index 1
//        globalState.updatePathDisplay(path.subList(1, path.size()));
    }

    private void moveToCrush() {
        // Logic to move to the fire location
        if (nextLocation == null) {
            nextLocation = (RoadTile) globalState.getGrid()[crushLocation[0]][crushLocation[1]];
            path = navigator.findPath(currentLocation, nextLocation);
            path = path.subList(0, path.size());
        }

//        System.out.println("[" + this.getLocalName() + "] Moving from " + currentLocation.getX() + " " + currentLocation.getY() + " to " + nextLocation.getX() + " " + nextLocation.getY() + " with path length " + path.size());

        int[] new_location = new int[2];
        new_location[0] = path.get(1).getX();
        new_location[1] = path.get(1).getY();

        globalState.moveCar(location[1], location[0], new_location[1], new_location[0], this);
        location = new_location;
        currentLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];

        if (location[0] == nextLocation.getX() && location[1] == nextLocation.getY()) {
            System.out.println("[" + this.getLocalName() + "] Arrived at crush location " + crushLocation[0] + ", " + crushLocation[1] + " starting to loading injured");
            currentState = State.LOADING_INJURED;
        }

        path = path.subList(1, path.size());

        // show path after the index 1
//        globalState.updatePathDisplay(path.subList(1, path.size()));
    }

    private void loadingInjured() {
        // Logic to extinguish fire
        RoadTile roadTile = (RoadTile) globalState.getGrid()[crushLocation[0]][crushLocation[1]];
        loadingProgress -= 50;
        System.out.println("[" + this.getLocalName() + "] Loading injured at " + crushLocation[0] + ", " + crushLocation[1] + " loading progress: " + loadingProgress + "%");
        if (loadingProgress <= 0) {
            System.out.println("[" + this.getLocalName() + "] Injured loaded, returning to fire station");
            loadingProgress = 100;
            roadTile.getAgent().doDelete();
            roadTile.setAgent(null);
            nextLocation = null;
            currentState = State.RETURNING_TO_HOSPITAL;
        }
    }

    private void returnToHospital() {
        // Logic to return to the fire station
        if (nextLocation == null) {
            nextLocation = getRoadTile((BuildingTile) globalState.getGrid()[stationLocation[0]][stationLocation[1]]);
            path = navigator.findPath(currentLocation, nextLocation);
        }

//        System.out.println("[" + this.getLocalName() + "] Moving from " + currentLocation.getX() + " " + currentLocation.getY() + " to " + nextLocation.getX() + " " + nextLocation.getY() + " with path length " + path.size());

        int[] new_location = new int[2];
        new_location[0] = path.get(1).getX();
        new_location[1] = path.get(1).getY();

        globalState.moveCar(location[1], location[0], new_location[1], new_location[0], this);
        location = new_location;
        currentLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];

        if (location[0] == nextLocation.getX() && location[1] == nextLocation.getY()) {
            System.out.println("[" + this.getLocalName() + "] Arrived at fire station " + stationLocation[0] + ", " + stationLocation[1]);
            currentState = State.ROAMING;
            nextLocation = null;
            isAddressingCrush = false;
            System.out.println("[" + this.getLocalName() + "] Returning to roaming state");
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(new jade.core.AID("Hosp", jade.core.AID.ISLOCALNAME));
            inform.setContent("Start Roaming");
            send(inform);
        }

        path = path.subList(1, path.size());

        // show path after the index 1
//        globalState.updatePathDisplay(path.subList(1, path.size()));
    }

    private int getDistanceToCrush(String locationString) {
        // Calculate distance based on the fire location and the truck's location
        System.out.println("[" + this.getLocalName() + "] Calculating distance to crush at " + locationString);
        crushLocation = new int[]{Integer.parseInt(locationString.split(", ")[0]), Integer.parseInt(locationString.split(", ")[1])};
        System.out.println("[" + this.getLocalName() + "] Crush location: " + crushLocation[0] + ", " + crushLocation[1]);
        RoadTile crushLocation = (RoadTile) globalState.getGrid()[this.crushLocation[0]][this.crushLocation[1]];
        RoadTile ambulanceLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];
        return navigator.findPath(ambulanceLocation, crushLocation).size();
    }

    private RoadTile getRoadTile(BuildingTile buildingTile) {
        RoadTile roadNextToBuilding = null;
        if (buildingTile.getX() > 0 && globalState.getGrid()[buildingTile.getX() - 1][buildingTile.getY()] instanceof RoadTile) {
            roadNextToBuilding = (RoadTile) globalState.getGrid()[buildingTile.getX() - 1][buildingTile.getY()];
        } else if (buildingTile.getX() < globalState.getGrid().length - 1 && globalState.getGrid()[buildingTile.getX() + 1][buildingTile.getY()] instanceof RoadTile) {
            roadNextToBuilding = (RoadTile) globalState.getGrid()[buildingTile.getX() + 1][buildingTile.getY()];
        } else if (buildingTile.getY() > 0 && globalState.getGrid()[buildingTile.getX()][buildingTile.getY() - 1] instanceof RoadTile) {
            roadNextToBuilding = (RoadTile) globalState.getGrid()[buildingTile.getX()][buildingTile.getY() - 1];
        } else if (buildingTile.getY() < globalState.getGrid()[0].length - 1 && globalState.getGrid()[buildingTile.getX()][buildingTile.getY() + 1] instanceof RoadTile) {
            roadNextToBuilding = (RoadTile) globalState.getGrid()[buildingTile.getX()][buildingTile.getY() + 1];
        }
        return roadNextToBuilding;
    }
}
