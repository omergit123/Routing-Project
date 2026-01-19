public class PredecessorAndDistance {
    private Router Predecessor;
    private double Distance;
    public PredecessorAndDistance(Router Predecessor, double Distance) {
        this.Predecessor = Predecessor;
        this.Distance = Distance;
    }
    public Router getPredecessor() {
        return Predecessor;
    }
    public double getDistance() {
        return Distance;
    }
    public void setPredecessor(Router Predecessor) {
        this.Predecessor = Predecessor;
    }
    public void setDistance(double Distance) {
        this.Distance = Distance;
    }
}
