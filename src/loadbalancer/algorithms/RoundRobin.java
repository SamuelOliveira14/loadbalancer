package loadbalancer.algorithms;
import loadbalancer.ServerConnection;
import java.util.List;

public class RoundRobin implements DistributionAlgorithm {
    
    private int currentIndex = 0;
    private List<ServerConnection> servers;

    public RoundRobin(List<ServerConnection> servers) {
        this.servers = servers;
    }

    @Override
    public ServerConnection nextServer() {
        currentIndex = (currentIndex + 1) % servers.size();
        return servers.get(currentIndex);
    }
    
}
