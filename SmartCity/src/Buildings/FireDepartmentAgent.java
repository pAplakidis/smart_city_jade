package Buildings;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.CyclicBehaviour;

import java.util.*;

public class FireDepartmentAgent extends Agent {
    private Map<AID, Integer> distances = new HashMap<>();
    private int numFiretrucks = 2;
    private List<AID> firetrucks = new ArrayList<>();
    // List that keeps track of unassigned fires
    private List<String> unassignedFires = new ArrayList<>();

    protected void setup() {
        firetrucks.add(new AID("FireTruck1", AID.ISLOCALNAME));
        firetrucks.add(new AID("FireTruck2", AID.ISLOCALNAME));

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mtInform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate mtPropose = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = myAgent.receive(mtInform);
                if (msg != null && msg.getContent().startsWith("Fire at ")) {
                    System.out.println("[" + getLocalName() + "] Received fire alert from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                    System.out.println("[" + getLocalName() + "] Unassigned fires: " + unassignedFires);
                    unassignedFires.add(msg.getContent().substring(8));
                    // Request distances from all firetrucks
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.setContent("Request distance for " + msg.getContent().substring(8));
                    for (AID firetruck : firetrucks) {
                        request.addReceiver(firetruck);
                    }
                    send(request);
                } else if (msg != null && msg.getContent().equals("Start Roaming")) {
                    if (!unassignedFires.isEmpty()) {
                        System.out.println("[" + getLocalName() + "] Received start roaming from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        System.out.println("[" + getLocalName() + "] Assigning truck to address fire at " + unassignedFires.get(0));
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        request.setContent("Request distance for " + unassignedFires.get(0));
                        for (AID firetruck : firetrucks) {
                            request.addReceiver(firetruck);
                        }
                        send(request);
                    }
                } else {
                    msg = myAgent.receive(mtPropose);
                    if (msg != null) {
                        if (msg.getContent().equals("9999")) {
                            System.out.println("[" + getLocalName() + "] "+ msg.getSender().getLocalName() +" is addressing fire");
                        }
                        distances.put(msg.getSender(), Integer.parseInt(msg.getContent()));
                        if (distances.size() == numFiretrucks) {
                            AID nearestTruck = distances.entrySet().stream()
                                    .min(Map.Entry.comparingByValue())
                                    .get()
                                    .getKey();
                            if (distances.get(nearestTruck) == 9999) {
                                System.out.println("[" + getLocalName() + "] All trucks are addressing fires");
                                distances.clear();
                                return;
                            }
                            System.out.println("[" + getLocalName() + "] Nearest truck is " + nearestTruck.getLocalName() + " at distance " + distances.get(nearestTruck));
                            System.out.println("[" + getLocalName() + "] Assigning truck to address fire at " + unassignedFires.get(0));
                            unassignedFires.remove(0);
                            ACLMessage assign = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            assign.addReceiver(nearestTruck);
                            assign.setContent("Address fire");
                            send(assign);
                            distances.clear();
                        }
                    } else {
                        block();
                    }
                }
            }
        });
    }
}
