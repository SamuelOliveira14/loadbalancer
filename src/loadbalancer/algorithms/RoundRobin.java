package loadbalancer.algorithms;
import loadbalancer.ServerConnection;
import java.util.List;

public class RoundRobin implements DistributionAlgorithm {
    
    private int currentIndex = 0;

    public RoundRobin() {
    }

    @Override
    public ServerConnection nextServer(List<ServerConnection> servers) {
        currentIndex = (currentIndex + 1) % servers.size();
        return servers.get(currentIndex);
    }

    public void reset() {
        currentIndex = 0;
    }
}
