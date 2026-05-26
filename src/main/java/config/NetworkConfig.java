package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class NetworkConfig {
    @JsonProperty("topology")
    public Topology topology;

    public static class Topology {
        public List<RouterConfig> routers;
        public List<LineConfig> links;
    }

    public static class RouterConfig {
        public String id;
        public double x;
        public double y;
    }

    public static class LineConfig {
        public String source;
        public String target;
        @JsonProperty("capacity_mbps")
        public double capacity;
    }
}