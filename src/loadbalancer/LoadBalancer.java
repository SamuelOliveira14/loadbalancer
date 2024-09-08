package loadbalancer;

import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

import loadbalancer.algorithms.DistributionAlgorithm;
import loadbalancer.algorithms.ListConnections;
import loadbalancer.algorithms.LowestLoad;
import loadbalancer.algorithms.RoundRobin;

public class LoadBalancer {

    private final int MAX_THREADS = 50;

    private DistributionAlgorithm algorithm;
    private ServerSocketChannel connectionListener = null;
    private Selector selector;
    private ExecutorService threadPool;
    private List<ServerConnection> workServers;

    public LoadBalancer(int port, String address, int maxQueue, DistributionAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        this.workServers = new ArrayList<>();
        createServerSocket(port, address, maxQueue);

    }

    public void addServer(String addr, int port) {
        workServers.add(new ServerConnection(port, addr));
        this.algorithm.reset();
    }

    private void createServerSocket(int port, String address, int maxQueue) {

        if (connectionListener != null)
            return;

        try {
            this.connectionListener = ServerSocketChannel.open();
            this.connectionListener.configureBlocking(false);
            this.connectionListener.bind(new InetSocketAddress(address, port), maxQueue);

            this.selector = Selector.open();

            this.connectionListener.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Listening on port " + port);
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    public void start() {
        while (true) {
            try {
                if (selector.select() == 0)
                    continue;

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        SocketChannel client = connectionListener.accept(); // connectionListener is the only channel registered for accepting connections

                        System.out.println("Connection accepted - " + client.getRemoteAddress().toString());

                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) { // handle client channels
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer request = ByteBuffer.allocate(Integer.BYTES).clear();
                        var bytesRead = client.read(request);

                        if (bytesRead == -1) {
                            client.close();
                            continue;
                        }

                        request.flip();
                        int requestValue =  request.getInt();
                        System.out.println("Got request value of " + requestValue);                        
                        
                        var clientAddress = client.getRemoteAddress().toString().replace("/", "").split(":")[0];
                        RequestPayload payload = new RequestPayload(clientAddress, requestValue);

                        threadPool.submit(() -> {
                            ByteBuffer response = algorithm.nextServer(workServers).request(payload);

                            try {
                                while (response.hasRemaining()) {
                                    client.write(response);
                                }
                                client.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    public static void main(String args[]) {
        DistributionAlgorithm algorithm = new RoundRobin();
        LoadBalancer lb = new LoadBalancer(8000, "localhost", 2000, algorithm);

        int numServers = args.length;
        ArrayList<String> workServers = new ArrayList<String>();

        for (int i = 0; i < numServers; ++i) {
            workServers.add(args[i]);
        }

        for (int i = 0; i < workServers.size(); ++i) {
            lb.addServer(workServers.get(i), 9000);
        }

        lb.start();
    }
}
