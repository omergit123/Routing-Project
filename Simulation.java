import java.util.HashMap;
import java.util.Scanner;

public class Simulation {
    public static void main(String[] args) {
        Network net = new Network(new HashMap<>());
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Main menu:\n\t1.Add router\n\t2.Add line\n\t3.Route\n\t4.Exit");
            int choice = scanner.nextInt();
            switch (choice){
                case 1:
                    System.out.println("Enter router ID: ");
                    int routerID = scanner.nextInt();
                    if(net.getRouter(routerID) == null) {
                        System.out.println("Enter x coordinate: ");
                        double x = scanner.nextDouble();
                        System.out.println("Enter y coordinate: ");
                        double y = scanner.nextDouble();
                        net.addRouter(new Router(x, y, routerID));
                        break;
                    }
                    System.out.println("ID is in use...");
                    break;
                case 2:
                    System.out.println("Enter router 1 ID: ");
                    int router1ID = scanner.nextInt();
                    System.out.println("Enter router 2 ID: ");
                    int router2ID = scanner.nextInt();
                    System.out.println("Enter capacity:");
                    double capacity = scanner.nextInt();
                    if(net.getRouter(router1ID) == null || net.getRouter(router2ID) == null || router1ID == router2ID
                        || capacity <= 0) {
                        System.out.println("ID's are not in use\\loop are not available.");
                        break;
                    }
                    net.getRouter(router1ID).addNeighbor(net.getRouter(router2ID), capacity);
                    break;
                case 3:
                    System.out.println("Enter source ID: ");
                    int sourceID = scanner.nextInt();
                    System.out.println("Enter destination ID: ");
                    int destinationID = scanner.nextInt();
                    System.out.println("Enter data load: ");
                    double dataLoad = scanner.nextDouble();
                    if(net.getRouter(sourceID) == null || net.getRouter(destinationID) == null || dataLoad <= 0) {
                        System.out.println("ID's are not in use\\data load is less than 0.");
                        break;
                    }
                    DijkstraAlgorithm alg = new DijkstraAlgorithm();
                    HashMap<Integer, PredecessorAndDistance> output=
                            alg.routing(net, net.getRouter(sourceID), dataLoad);
                    System.out.println("Routing Time: " + output.get(destinationID).getDistance() + " seconds.");
                    if(output.get(destinationID).getDistance()<Double.POSITIVE_INFINITY) {
                        System.out.println("Route: " + printPath(destinationID, output));
                    }
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
    public static String printPath(Integer ID,  HashMap<Integer, PredecessorAndDistance> output) {
        if (output.get(ID).getDistance()>0) {
            return printPath(output.get(ID).getPredecessor().getId(), output) + "->" + ID;
        }
        return ID.toString();
    }
}
