package loadbalancer.server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class WorkServer {
    private ServerSocketChannel channel;
    private Selector selector;
    private final int maxThreads;
    private ArrayList<Thread> threads;

    public WorkServer(String addr, int port, int maxThreads) {
        this.maxThreads = maxThreads;

        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(addr, port));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {

        }
    }

    public void run() {
        while (true) {
            handleThreads();
            handleRequests();
        }
    }

    private void handleThreads() {
        for (int i = 0; i < threads.size(); ++i) {
            if (!threads.get(i).isAlive()) {
                // thread has finished
                // TODO: reply to loadBalancer with answer, remove thread
            }
        }
    }

    private void handleRequests() {
        // https://stackoverflow.com/questions/3895461/non-blocking-sockets
        try {
            selector.select(1); // block up to 1 ms

            // TODO: handle skipping keys when server is busy
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
        
                if (!key.isValid()) {
                    keys.remove();
                    continue;
                }

                // we only care about reading
                if (key.isReadable()) {
                    if (threads.size() < maxThreads) {
                        System.out.println("Reading connection");
                        // TODO: add working thread with the request
                        keys.remove();
                    } else {
                        System.out.println("WorkServer is too busy...");
                    }
                }
            }
        } catch (Exception e) {

        }
    }
}
