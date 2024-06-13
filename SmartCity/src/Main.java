import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Set up the JADE environment
        jade.core.Runtime rt = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");

        // Create the main container
        AgentContainer mainContainer = rt.createMainContainer(profile);

        try {
            // Initialize BuildingAgent
            AgentController building = mainContainer.createNewAgent("Building", "Buildings.BuildingAgent", null);
            building.start();

            // Initialize FireDepartmentAgent
            AgentController fireDept = mainContainer.createNewAgent("FireDept", "Buildings.FireDepartmentAgent", null);
            fireDept.start();

            // Initialize FiretruckAgent with arguments
            Object[] fireTruck1Args = {199, 0, 2};
            AgentController fireTruck1 = mainContainer.createNewAgent("FireTruck1", "Vehicles.FiretruckAgent", fireTruck1Args);
            fireTruck1.start();

            Object[] fireTruck2Args = {199, 13, 14};
            AgentController fireTruck2 = mainContainer.createNewAgent("FireTruck2", "Vehicles.FiretruckAgent", fireTruck2Args);
            fireTruck2.start();

            // Initialize CarAgent with arguments
            Object[] car1Args = {5, -1, -1, 1};
            AgentController car1 = mainContainer.createNewAgent("Car1", "Vehicles.CarAgent", car1Args);
            car1.start();

            Object[] car2Args = {6, 11, 6, 1};
            AgentController car2 = mainContainer.createNewAgent("Car2", "Vehicles.CarAgent", car2Args);
            car2.start();

            Object[] car3Args = {7, -1, -1, 1};
            AgentController car3 = mainContainer.createNewAgent("Car3", "Vehicles.CarAgent", car3Args);
            car3.start();

            Object[] car4Args = {8, -1, -1, 1};
            AgentController car4 = mainContainer.createNewAgent("Car4", "Vehicles.CarAgent", car4Args);
            car4.start();

            Object[] car5Args = {9, -1, -1, 1};
            AgentController car5 = mainContainer.createNewAgent("Car5", "Vehicles.CarAgent", car5Args);
            car5.start();

            // Initialize HospitalAgent
            AgentController hosp = mainContainer.createNewAgent("Hosp", "Buildings.HospitalAgent", null);
            hosp.start();

            // Initialize AmbulanceAgent
            AgentController ambu1 = mainContainer.createNewAgent("Ambu1", "Vehicles.AmbulanceAgent", null);
            ambu1.start();

            Object[] ambu2Args = {180, 0, 20};
            AgentController ambu2 = mainContainer.createNewAgent("Ambu2", "Vehicles.AmbulanceAgent", ambu2Args);
            ambu2.start();

            while (true) {
                // Definitely the best way to check if an agent is deleted ;)
                try {
                    car1.getState().toString();
                } catch (Exception e) {
                    car1 = mainContainer.createNewAgent("Car1", "Vehicles.CarAgent", car1Args);
                    car1.start();
                }
                try {
                    car2.getState().toString();
                } catch (Exception e) {
                    car2 = mainContainer.createNewAgent("Car2", "Vehicles.CarAgent", car2Args);
                    car2.start();
                }
                try {
                    car3.getState().toString();
                } catch (Exception e) {
                    car3 = mainContainer.createNewAgent("Car3", "Vehicles.CarAgent", car3Args);
                    car3.start();
                }
                try {
                    car4.getState().toString();
                } catch (Exception e) {
                    car4 = mainContainer.createNewAgent("Car4", "Vehicles.CarAgent", car4Args);
                    car4.start();
                }
                try {
                    car5.getState().toString();
                } catch (Exception e) {
                    car5 = mainContainer.createNewAgent("Car5", "Vehicles.CarAgent", car5Args);
                    car5.start();
                }
                TimeUnit.SECONDS.sleep(1);
            }


        } catch (StaleProxyException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}