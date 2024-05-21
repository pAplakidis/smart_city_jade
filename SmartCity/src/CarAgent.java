import Behaviours.RoamBehaviour;
import State.GlobalState;
import jade.core.Agent;

public class CarAgent extends Agent {
    private int[] location = new int[]{0, 0};
    private int id = 5;
    private GlobalState globalState;

    public int[] getLocation(){
        return location;
    }

    public void setLocation(int[] newLocation){
        location = newLocation;
    }

    public void setup(){
        globalState = GlobalState.getInstance();
        System.out.println("[CarAgent] Accessed Grid");

        globalState.setCarLocation(location[0], location[1], id);
        globalState.showGrid();

        addBehaviour(new RoamBehaviour(this, location, id));
    }

    public void move(int x, int y){
        location[0] = x;
        location[1] = y;
        globalState.setCarLocation(location[0], location[1], id);
    }
}

