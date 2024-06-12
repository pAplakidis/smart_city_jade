package Buildings;

import State.GlobalState;
import Tiles.BuildingTile;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class BuildingAgent extends Agent {
    private double fireProbability = 0.05; // 1% chance of fire
    private GlobalState globalState;
    private List<BuildingTile> buildings;

    protected void setup() {
        globalState = GlobalState.getInstance();

        // Get all buildings
        buildings = globalState.getBuildings();


        addBehaviour(new TickerBehaviour(this, 500) { // checks every second
            protected void onTick() {
                int randomIndex = (int) (Math.random() * buildings.size());
                BuildingTile randomTile = buildings.get(randomIndex);
                int[] location = new int[]{randomTile.getX(), randomTile.getY()};
                if (Math.random() < fireProbability && !globalState.isOnFire(location[1], location[0])) {
                    System.out.println("[" + getLocalName() + "] Fire at " + getLocalName() + " (" + location[0] + ", " + location[1] + ")");
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new jade.core.AID("FireDept", jade.core.AID.ISLOCALNAME));
                    msg.setContent("Fire at location " + location[0] + ", " + location[1]);
                    globalState.setOnFire(location[1], location[0]);
                    send(msg);
                }
            }
        });
    }
}
