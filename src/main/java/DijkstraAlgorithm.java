
import java.util.*;

public class DijkstraAlgorithm implements RoutingAlgorithm {
    private HashMap<String, PredecessorAndDistance> parentAndDistance;
    private double load;

    // This class identify each Router by distance, for algorithm implementation.
    private static class RouterNode implements Comparable<RouterNode> {
        Router router;
        double distance;

        public RouterNode(Router router, double distance) {
            this.router = router;
            this.distance = distance;
        }

        public int compareTo(RouterNode o) {
            return Double.compare(distance, o.distance);
        }
    }

    // Initializes the Dijkstra's algorithm by setting the initial distances and
    // predecessors for each router in the network.
    private void init(Network network, Router source, double data_load) {
        load = data_load;
        parentAndDistance = new HashMap<>();
        network.getRouters().forEach((id, router) -> {
            parentAndDistance.put(id, new PredecessorAndDistance(null, Double.POSITIVE_INFINITY));
        });
        parentAndDistance.get(source.getId()).setDistance(0);
    }

    // Relaxation function for Dijkstra's algorithm. It updates the distance and
    // predecessor if a shorter path is found.
    private boolean relax(Line line) {
        String router1 = line.getRouter1().getId();
        String router2 = line.getRouter2().getId();
        if (parentAndDistance.get(router2).getDistance() > parentAndDistance.get(router1).getDistance()
                + line.getWeight(load)) {
            parentAndDistance.get(router2).setDistance(parentAndDistance.get(router1).getDistance()
                    + line.getWeight(load));
            parentAndDistance.get(router2).setPredecessor(line.getRouter1());
            return true;
        }
        return false;
    }

    // Main routing function for Dijkstra's algorithm. It returns the shortest paths
    // from the source router to all other routers in the network.
    public HashMap<String, PredecessorAndDistance> routing(Network network, Router source, double load) {
        init(network, source, load);
        PriorityQueue<RouterNode> queue = new PriorityQueue<>();
        queue.add(new RouterNode(source, 0));
        while (!queue.isEmpty()) {
            RouterNode node = queue.poll();
            if (parentAndDistance.get(node.router.getId()).getDistance() < node.distance) {
                continue;
            }
            for (Line line : node.router.getNeighbors()) {
                if (relax(line)) {
                    queue.add(new RouterNode(line.getRouter2(), line.getWeight(load)));
                    line.setCurrentLoad(line.getCurrentLoad() + load);
                }
            }
        }
        return parentAndDistance;
    }
}
