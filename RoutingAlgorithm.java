import java.util.HashMap;

public interface RoutingAlgorithm {
    public HashMap<Integer, PredecessorAndDistance> routing(Network network, Router source, double load);
}
