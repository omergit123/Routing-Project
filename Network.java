import java.util.HashMap;

public class Network {
    private HashMap<Integer, Router> routers;
    public Network(HashMap<Integer, Router> vertices) {
        this.routers = vertices;
    }
    public void addRouter(Router v) {
        routers.put(v.getId(), v);
    }
    public Router getRouter(int id) {
        return routers.get(id);
    }
    public HashMap<Integer, Router> getRouters() {
        return routers;
    }

}
