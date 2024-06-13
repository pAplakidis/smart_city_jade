package Buildings;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalAgent extends Agent {
    private Map<AID, Integer> distances = new HashMap<>();
    private int numAmbulances = 2;
    private List<AID> ambulance = new ArrayList<>();
    // List that keeps track of unassigned fires
    private List<String> unassignedCrashes = new ArrayList<>();

    protected void setup() {
        ambulance.add(new AID("Ambu1", AID.ISLOCALNAME));
        ambulance.add(new AID("Ambu2", AID.ISLOCALNAME));

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mtInform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate mtPropose = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = myAgent.receive(mtInform);
                if (msg != null && msg.getContent().startsWith("Crush at ")) {
                    System.out.println("[" + getLocalName() + "] Received crashed from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                    System.out.println("[" + getLocalName() + "] Unassigned crashes: " + unassignedCrashes);
                    unassignedCrashes.add(msg.getContent().substring(9));
                    // Request distances from all firetrucks
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.setContent("Request distance for " + msg.getContent().substring(9));
                    for (AID firetruck : ambulance) {
                        request.addReceiver(firetruck);
                    }
                    send(request);
                } else if (msg != null && msg.getContent().equals("Start Roaming")) {
                    if (!unassignedCrashes.isEmpty()) {
                        System.out.println("[" + getLocalName() + "] Received start roaming from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        System.out.println("[" + getLocalName() + "] Assigning ambulance to address crush at " + unassignedCrashes.get(0));
                        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                        request.setContent("Request distance for " + unassignedCrashes.get(0));
                        for (AID firetruck : ambulance) {
                            request.addReceiver(firetruck);
                        }
                        send(request);
                    }
                } else {
                    msg = myAgent.receive(mtPropose);
                    if (msg != null) {
                        if (msg.getContent().equals("9999")) {
                            System.out.println("[" + getLocalName() + "] " + msg.getSender().getLocalName() + " is addressing a crash");
                        }
                        distances.put(msg.getSender(), Integer.parseInt(msg.getContent()));
                        if (distances.size() == numAmbulances) {
                            AID nearestTruck = distances.entrySet().stream()
                                    .min(Map.Entry.comparingByValue())
                                    .get()
                                    .getKey();
                            if (distances.get(nearestTruck) == 9999) {
                                System.out.println("[" + getLocalName() + "] All ambulances are addressing fires");
                                System.out.println("[" + getLocalName() + "] Unassigned crashes: " + unassignedCrashes);
                                distances.clear();
                                return;
                            }
                            System.out.println("[" + getLocalName() + "] Nearest ambulance is " + nearestTruck.getLocalName() + " at distance " + distances.get(nearestTruck));
                            System.out.println("[" + getLocalName() + "] Assigning ambulance to address crash at " + unassignedCrashes.get(0));
                            unassignedCrashes.remove(0);
                            ACLMessage assign = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            assign.addReceiver(nearestTruck);
                            assign.setContent("Address crash");
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
