public class Line {
    private Router router1;
    private Router router2;
    private double delay;
    private double capacity;
    private double currentLoad;
    final int SPEED = 200000;
    public Line(Router router1, Router router2, double capacity) {
        this.router1 = router1;
        this.router2 = router2;
        this.capacity = capacity;
        delay = router1.getDistance(router2) / SPEED;
        currentLoad = 0;
    }
    // weight function - by delay and considering the current load.
    public double getWeight(double load) {
        if (load > capacity - currentLoad) {
            return Double.POSITIVE_INFINITY;
        }
        return delay + 1/(capacity - currentLoad);
    }
    public Router getRouter1() {
        return router1;
    }
    public Router getRouter2() {
        return router2;
    }
    public void setCurrentLoad(double load) {
        currentLoad = load;
    }
    public double getCurrentLoad() {
        return currentLoad;
    }
}
