package loadbalancer;

public interface DistributionAlgorithm {
    
    public ServerConnection nextServer();
}
