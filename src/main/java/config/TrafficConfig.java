package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TrafficConfig {
    @JsonProperty("streams")
    public List<Stream> streams;

    public static class Stream {
        @JsonProperty("stream_id")
        public String streamId;
        public String source;
        public String destination;
        @JsonProperty("size_mb")
        public double sizeMb;
    }
}