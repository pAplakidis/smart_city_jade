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

import javax.swing.*;
import java.util.List;

public class FiretruckAgent extends CarAgent {
    private int id = 1;
    private int[] location = new int[]{2, 0};
    private int[] fireLocation = new int[]{0, 0};
    private int[] stationLocation = new int[]{10, 8};
    private boolean isAddressingFire = false;
    private int firePercentage = 100;
    private List<Tile> path = null;
    private GlobalState globalState;
    private Navigator navigator;
    private RoadTile currentLocation;
    private RoadTile nextLocation;

    private enum State {
        ROAMING,
        MOVING_TO_FIRE,
        EXTINGUISHING_FIRE,
        RETURNING_TO_STATION
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
                    if (isAddressingFire) {
                        reply.setContent("9999");
                        send(reply);
                    } else {
                        System.out.println("[" + getLocalName() + "] Received and Answering distance request from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        String locationString = msg.getContent().substring(30);
                        reply.setContent(String.valueOf(getDistanceToFire(locationString)));
                        send(reply);
                    }
                } else {
                    msg = myAgent.receive(mtAssign);
                    if (msg != null) {
                        System.out.println("[" + getLocalName() + "] Addressing fire at location " + fireLocation[0] + ", " + fireLocation[1]);
                        isAddressingFire = true;
                        nextLocation = null;
                        currentState = State.MOVING_TO_FIRE;
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
                    case MOVING_TO_FIRE:
                        moveToFire();
                        break;
                    case EXTINGUISHING_FIRE:
                        extinguishFire();
                        break;
                    case RETURNING_TO_STATION:
                        returnToStation();
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

    private void moveToFire() {
        // Logic to move to the fire location
        if (nextLocation == null) {
            nextLocation = getRoadTile((BuildingTile) globalState.getGrid()[fireLocation[0]][fireLocation[1]]);
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
            System.out.println("[" + this.getLocalName() + "] Arrived at fire location " + fireLocation[0] + ", " + fireLocation[1] + " starting to extinguish fire");
            currentState = State.EXTINGUISHING_FIRE;
        }

        path = path.subList(1, path.size());

        // show path after the index 1
//        globalState.updatePathDisplay(path.subList(1, path.size()));
    }

    private void extinguishFire() {
        // Logic to extinguish fire
        BuildingTile fireBuilding = (BuildingTile) globalState.getGrid()[fireLocation[0]][fireLocation[1]];
        firePercentage -= 25;
        System.out.println("[" + this.getLocalName() + "] Extinguishing fire at " + fireLocation[0] + ", " + fireLocation[1] + " remaining fire " + firePercentage + "%");
        if (firePercentage <= 0) {
            fireBuilding.setOnFire(false);
            System.out.println("[" + this.getLocalName() + "] Fire extinguished at " + fireLocation[0] + ", " + fireLocation[1]);
            firePercentage = 100;
            nextLocation = null;
            currentState = State.RETURNING_TO_STATION;
        }
    }

    private void returnToStation() {
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
            isAddressingFire = false;
            System.out.println("[" + this.getLocalName() + "] Returning to roaming state");
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(new jade.core.AID("FireDept", jade.core.AID.ISLOCALNAME));
            inform.setContent("Start Roaming");
            send(inform);
        }

        path = path.subList(1, path.size());

        // show path after the index 1
//        globalState.updatePathDisplay(path.subList(1, path.size()));
    }

    private int getDistanceToFire(String locationString) {
        // Calculate distance based on the fire location and the truck's location
        System.out.println("[" + this.getLocalName() + "] Calculating distance to fire at " + locationString);
        fireLocation = new int[]{Integer.parseInt(locationString.split(", ")[0]), Integer.parseInt(locationString.split(", ")[1])};
        System.out.println("[" + this.getLocalName() + "] Fire location: " + fireLocation[0] + ", " + fireLocation[1]);
        BuildingTile fireBuilding = (BuildingTile) globalState.getGrid()[fireLocation[0]][fireLocation[1]];
        RoadTile fireTruckLocation = (RoadTile) globalState.getGrid()[location[0]][location[1]];
        RoadTile roadNextToFire = getRoadTile(fireBuilding);
        return navigator.findPath(fireTruckLocation, roadNextToFire).size();
    }

    private RoadTile getRoadTile(BuildingTile fireBuilding) {
        RoadTile roadNextToFire = null;
        if (fireBuilding.getX() > 0 && globalState.getGrid()[fireBuilding.getX() - 1][fireBuilding.getY()] instanceof RoadTile) {
            roadNextToFire = (RoadTile) globalState.getGrid()[fireBuilding.getX() - 1][fireBuilding.getY()];
        } else if (fireBuilding.getX() < globalState.getGrid().length - 1 && globalState.getGrid()[fireBuilding.getX() + 1][fireBuilding.getY()] instanceof RoadTile) {
            roadNextToFire = (RoadTile) globalState.getGrid()[fireBuilding.getX() + 1][fireBuilding.getY()];
        } else if (fireBuilding.getY() > 0 && globalState.getGrid()[fireBuilding.getX()][fireBuilding.getY() - 1] instanceof RoadTile) {
            roadNextToFire = (RoadTile) globalState.getGrid()[fireBuilding.getX()][fireBuilding.getY() - 1];
        } else if (fireBuilding.getY() < globalState.getGrid()[0].length - 1 && globalState.getGrid()[fireBuilding.getX()][fireBuilding.getY() + 1] instanceof RoadTile) {
            roadNextToFire = (RoadTile) globalState.getGrid()[fireBuilding.getX()][fireBuilding.getY() + 1];
        }
        return roadNextToFire;
    }
}