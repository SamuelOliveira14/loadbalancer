package loadbalancer;

import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

import loadbalancer.algorithms.DistributionAlgorithm;

public class LoadBalancer {

    private final int MAX_THREADS = 50;

    private DistributionAlgorithm algorithm;
    private ServerSocketChannel connectionListener = null;
    private Selector selector;
    private ExecutorService threadPool;

    public LoadBalancer(int port, String address, int maxQueue, DistributionAlgorithm algorithm, List<ServerConnection> servers) {
        
        this.algorithm = algorithm;
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        createServerSocket(port, address, maxQueue);

    }

    private void createServerSocket(int port, String address, int maxQueue) {
        
        if(connectionListener != null) return;

        try {
            this.connectionListener = ServerSocketChannel.open();
            this.connectionListener.configureBlocking(false);
            this.connectionListener.bind(new InetSocketAddress(address, port), maxQueue);
            
            this.selector = Selector.open();

            this.connectionListener.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (Exception e) {
            throw new RuntimeException();
        }

    }

    public void start() {

        while(true){
            try {
                if(selector.select() == 0) continue;
                
                for(SelectionKey key : selector.selectedKeys()){

                    if(key.isAcceptable() && key.channel() instanceof ServerSocketChannel){

                        SocketChannel client = connectionListener.accept(); //connectionListener is the only channel registered for accepting connections

                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        
                    }else if(key.isReadable() && key.channel() instanceof SocketChannel){ //handle client channels
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer request = ByteBuffer.allocate(Integer.BYTES).clear();
                        var bytesRead = client.read(request);
                        
                        if(bytesRead == -1){
                            client.close();
                            continue;
                        }
                        
                        Payload payload = new Payload(client.getLocalAddress().toString(), request.getInt());
                        
                        threadPool.submit(() -> {
                                ByteBuffer response = algorithm.nextServer().request(payload);

                                try{
                                    while (response.hasRemaining()) client.write(response);
                                } catch (Exception e){
                                    throw new RuntimeException();
                                }

                            }
                        );

                        client.close();
                    }

                    selector.selectedKeys().remove(key);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    protected void finalize() {
        try {
            // close resources...
        } catch (Exception e) {

        }
    }

}
