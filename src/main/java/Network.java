
import config.*;
import java.util.HashMap;

public class Network {
    private HashMap<String, Router> routers;

    public Network(HashMap<String, Router> vertices) {
        this.routers = vertices;
    }

    // Added constructor to build the network from NetworkConfig
    public Network(NetworkConfig config) {
        this.routers = new HashMap<>();
        for (NetworkConfig.RouterConfig routerConfig : config.topology.routers) {
            Router router = new Router(routerConfig.x, routerConfig.y, routerConfig.id);
            addRouter(router);
        }
        for (NetworkConfig.LineConfig lineConfig : config.topology.links) {
            Router source = getRouter(lineConfig.source);
            Router target = getRouter(lineConfig.target);
            if (source != null && target != null) {
                source.addNeighbor(target, lineConfig.capacity);
            }
        }
    }

    public void addRouter(Router v) {
        routers.put(v.getId(), v);
    }

    public Router getRouter(String id) {
        return routers.get(id);
    }

    public HashMap<String, Router> getRouters() {
        return routers;
    }

}
