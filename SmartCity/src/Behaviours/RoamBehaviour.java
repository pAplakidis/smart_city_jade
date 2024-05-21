package Behaviours;

import State.GlobalState;
import Tiles.Tile;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class RoamBehaviour extends Behaviour {
    private GlobalState globalState;
    private int[] agentLocation;
    private int agentId;

    public RoamBehaviour(Agent a, int[] agentLocation, int agentId){
        super(a);
        this.globalState = GlobalState.getInstance();
        this.agentLocation = agentLocation;
        this.agentId = agentId;
    }

    // TODO: calculate valid tiles to move on (in bounds, not building, in lane, intersection)
    private void getValidTiles(){

    }

    @Override
    public void action() {
        int x = agentLocation[0];
        int y = agentLocation[1];

        // TODO: not using the actual x,y locations
        int newX = x;
        int newY = y + 1;

        agentLocation[0] = newX;
        agentLocation[1] = newY;

        globalState.moveCar(x, y, newX, newY, agentId);
        globalState.showGrid();
    }

    @Override
    public boolean done() {
        return true;
    }
}
