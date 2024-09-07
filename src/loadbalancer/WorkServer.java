package loadbalancer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.*;

public class WorkServer {
    private final int MAX_THREADS = 100;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

    public WorkServer(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                if (selector.select() == 0)
                    continue;

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        System.out.println("Accepted connection from client " + client.getRemoteAddress());
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer request = ByteBuffer.allocate(2*Integer.BYTES).clear();
                        int bytesRead = client.read(request);

                        if (bytesRead == -1) {
                            client.close();
                            continue;
                        }

                        request.flip();

                        threadPool.submit(()->{
                            try {
                                RequestPayload payload = new RequestPayload(request);
                                var response = handleRequest(payload).toByteBuffer();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ResponsePayload handleRequest(RequestPayload p) {
        System.out.println("Handling request of time " + p.getTime());
        int returnVal = 0;
        try {
            TimeUnit.SECONDS.sleep(p.getTime());
            returnVal = p.getTime() * 2 + 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Finished, returning value is: " + returnVal);
        var res = new ResponsePayload(p.getIP(), returnVal);
        return res;
    }

    public static void main(String[] args) {
        var server = new WorkServer(Integer.parseInt(args[0]));
        server.run();
    }
}
