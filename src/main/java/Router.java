
import java.util.Collection;
import java.util.HashMap;

public class Router {
    private String id;
    private double x;
    private double y;
    private HashMap<String, Line> neighbors;

    public Router(double x, double y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
        neighbors = new HashMap<>();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDistance(Router v) {
        return Math.sqrt(Math.pow(this.x - v.getX(), 2) + Math.pow(this.y - v.getY(), 2));
    }

    public String getId() {
        return id;
    }

    public void addNeighbor(Router router, double capacity) {
        neighbors.put(router.getId(), new Line(this, router, capacity));
    }

    public Collection<Line> getNeighbors() {
        return neighbors.values();
    }

    public Line getNeighbor(String id) {
        return neighbors.get(id);
    }
}
