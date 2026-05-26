
import java.util.HashMap;

public interface RoutingAlgorithm {
    public HashMap<String, PredecessorAndDistance> routing(Network network, Router source, double load);
}
