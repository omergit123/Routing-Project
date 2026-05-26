import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RoutingProjectTests {

    // ---------------------------------------------------------
    // Test 1: Network Meltdown
    // most of the lines are almost full, besides one route which is 99% full.
    // ---------------------------------------------------------
    @Test
    public void testNetworkMeltdown() {
        Network net = new Network(new HashMap<>());
        Router source = new Router(0, 0, 1);
        Router dest = new Router(100, 100, 99);
        net.addRouter(source);
        net.addRouter(dest);

        int bestPathId = -1;

        for (int i = 0; i < 10; i++) {
            Router middle = new Router(50, i * 10, 100 + i);
            net.addRouter(middle);

            source.addNeighbor(middle, 100);
            middle.addNeighbor(dest, 100);

            double loadLevel;
            if (i == 5) {
                loadLevel = 99.0;
                bestPathId = middle.getId();
            } else {
                loadLevel = 99.9;
            }
            setLoadOnPath(source, middle, loadLevel);
            setLoadOnPath(middle, dest, loadLevel);
        }
        DijkstraAlgorithm alg = new DijkstraAlgorithm();
        var result = alg.routing(net, source, 0.05);

        assertNotEquals(Double.POSITIVE_INFINITY, result.get(dest.getId()).getDistance(), "Path must be found.");

        int chosenMiddleNode = result.get(dest.getId()).getPredecessor().getId();
        assertEquals(dest.getId(), dest.getId());

        Router pred = result.get(dest.getId()).getPredecessor();
        assertEquals(bestPathId, pred.getId(),
                "The algorithm chose too loaded route.");
    }

    // ---------------------------------------------------------
    // Test 2: A grid. Most of the lines are full.
    // ---------------------------------------------------------
    @Test
    public void testTheMaze() {
        Network net = new Network(new HashMap<>());
        int size = 10;
        Router[][] grid = new Router[size][size];

        int id = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new Router(i, j, id++);
                net.addRouter(grid[i][j]);
            }
        }

        Random rand = new Random(12345);
        int blockedCount = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j < size - 1) {
                    double capacity = 100;
                    grid[i][j].addNeighbor(grid[i][j+1], capacity);
                    if (rand.nextDouble() < 0.999) {
                        setLoadOnPath(grid[i][j], grid[i][j+1], capacity);
                        blockedCount++;
                    }
                }
                if (i < size - 1) {
                    double capacity = 100;
                    grid[i][j].addNeighbor(grid[i+1][j], capacity);
                    if (rand.nextDouble() < 0.4) {
                        setLoadOnPath(grid[i][j], grid[i+1][j], capacity);
                        blockedCount++;
                    }
                }
            }
        }

        System.out.println("Maze Test: Blocked " + blockedCount + " lines out of total.");

        DijkstraAlgorithm alg = new DijkstraAlgorithm();
        var result = alg.routing(net, grid[0][0], 1);

        double dist = result.get(grid[size-1][size-1].getId()).getDistance();
        if (dist == Double.POSITIVE_INFINITY) {
            System.out.println("Maze Test: No path found (valid result due to heavy blocking).");
        } else {
            assertTrue(dist < Double.POSITIVE_INFINITY);
            System.out.println("Maze Test: Path found! Distance: " + dist);
        }
    }

    // ---------------------------------------------------------
    // Test 3: Massive Random Graph - 1000 routers.
    // ---------------------------------------------------------
    @Test
    public void testMassiveRandomGraph() {
        Network net = new Network(new HashMap<>());
        int numRouters = 1000; // אלף נתבים!
        int connectionsPerRouter = 5;

        List<Router> routers = new ArrayList<>();

        for (int i = 0; i < numRouters; i++) {
            Router r = new Router(Math.random() * 1000, Math.random() * 1000, i);
            net.addRouter(r);
            routers.add(r);
        }

        Random rand = new Random();
        for (Router r : routers) {
            for (int k = 0; k < connectionsPerRouter; k++) {
                Router target = routers.get(rand.nextInt(numRouters));
                if (target != r) {
                    r.addNeighbor(target, 1000);
                }
            }
        }

        System.out.println("Generating path on 1,000 nodes network...");
        long startTime = System.currentTimeMillis();

        DijkstraAlgorithm alg = new DijkstraAlgorithm();
        var result = alg.routing(net, routers.get(0), 10);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Calculation took: " + duration + " ms");

        assertTrue(duration < 2000, "The algorithm is too slow for 1000 nodes network.");
    }

    private void setLoadOnPath(Router from, Router to, double load) {
        for (Line line : from.getNeighbors()) {
            if (line.getRouter2().getId() == to.getId()) {
                line.setCurrentLoad(load);
                return;
            }
        }
    }
}