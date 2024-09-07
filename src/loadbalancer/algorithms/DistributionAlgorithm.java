package loadbalancer.algorithms;

import loadbalancer.ServerConnection;

public interface DistributionAlgorithm {
    
    public ServerConnection nextServer();
}
