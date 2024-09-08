package loadbalancer.algorithms;

import java.util.List;

import loadbalancer.ServerConnection;

public class LowestLoad implements DistributionAlgorithm {

    @Override
    public ServerConnection nextServer(List<ServerConnection> servers) {
        ServerConnection returnServer = servers.get(0);
        int lowestLoad = servers.get(0).getLoad();
        for (int i = 1; i < servers.size(); ++i) {
            if (servers.get(i).getLoad() < lowestLoad) {
                lowestLoad = servers.get(i).getLoad();
                returnServer = servers.get(i);
            }
        }
        return returnServer;
    }

    @Override
    public void reset() {
        // Do nothing
    }
}
