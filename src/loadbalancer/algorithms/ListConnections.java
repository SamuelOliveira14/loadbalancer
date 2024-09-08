package loadbalancer.algorithms;

import java.util.List;

import loadbalancer.ServerConnection;

public class ListConnections implements DistributionAlgorithm {
    @Override
    public ServerConnection nextServer(List<ServerConnection> servers) {
        ServerConnection returnServer = servers.get(0);
        int lowestNumConnections = servers.get(0).getNumConnections();
        for (int i = 1; i < servers.size(); ++i) {
            if (servers.get(i).getNumConnections() < lowestNumConnections) {
                lowestNumConnections = servers.get(i).getNumConnections();
                returnServer = servers.get(i);
            }
        }
        return returnServer;
    }

    @Override
    public void reset() {
        // Nothing
    }
}
