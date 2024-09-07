package loadbalancer.algorithms;

import java.util.List;

import loadbalancer.ServerConnection;

public interface DistributionAlgorithm {
    
    public ServerConnection nextServer(List<ServerConnection> servers);

    public void reset();
}
