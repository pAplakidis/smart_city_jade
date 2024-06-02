package Behaviours;

import State.GlobalState;
import State.Navigator;
import Tiles.RoadTile;
import Tiles.Tile;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import java.util.List;
import java.util.concurrent.TimeUnit;

// TODO: cleanup
public class RoamBehaviour extends Behaviour {
    private GlobalState globalState;
    private int[] agentLocation;
    private int agentId;

    private Navigator navigator;
    private int newX, newY;
    private List<Tile> path;
    private int pathIdx;
    private boolean navDone;

    // TODO: this is not a roam but a goTo
    // TODO: pick random point (check if valid)
    public RoamBehaviour(Agent a, int[] agentLocation, int agentId, int newX, int newY){
        super(a);

        // init variables
        this.globalState = GlobalState.getInstance();
        this.agentLocation = agentLocation;
        this.agentId = agentId;
        navigator = new Navigator(globalState.getGrid());
        navDone = false;
        this.newX = newX;
        this.newY = newY;

        // get path
        int x = agentLocation[0];
        int y = agentLocation[1];
        Tile[][] grid = globalState.getGrid();
        RoadTile start = (RoadTile) grid[y][x];
        RoadTile end = (RoadTile) grid[newY][newX];
        path = navigator.findPath(start, end);
        pathIdx = 0;
        globalState.updatePathDisplay(path);
    }

    @Override
    public void action() {
        int x = agentLocation[0];
        int y = agentLocation[1];

        if (pathIdx > path.size()) {
            navDone = true;
            return;
        }

        // NOTE: here x and y are columns and rows correspondingly (i.e. they are inverted)
        int xPath = path.get(pathIdx).getY();
        int yPath = path.get(pathIdx).getX();

        // TODO: set location of CarAgent as well
        agentLocation[0] = xPath;
        agentLocation[1] = yPath;
        globalState.moveCar(x, y, xPath, yPath, agentId);

        if(agentLocation[0] == newX && agentLocation[1] == newY){
            navDone = true;
            return;
        }
        pathIdx++;
        myAgent.doWait(1000);
    }

    @Override
    public boolean done() {
        return navDone;
    }
}
