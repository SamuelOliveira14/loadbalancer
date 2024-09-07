package loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WorkClientTest {
    public static void main(String[] args) throws IOException {
        // Step 1: Open a ServerSocketChannel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        
        // Step 2: Bind to a port (e.g., 8080) and configure as non-blocking
        serverChannel.bind(new InetSocketAddress(9876));
        serverChannel.configureBlocking(false);

        // Step 3: Open a Selector
        Selector selector = Selector.open();

        // Step 4: Register the ServerSocketChannel with the Selector for accepting connections
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started, waiting for connections...");

        while (true) {
            // Step 5: Wait for events (blocking until an event occurs)
            selector.select();

            // Step 6: Get the selection keys (set of events)
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            // Step 7: Process each selected key
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                // Step 8: Check if it's an incoming connection
                if (key.isAcceptable()) {
                    handleAccept(key);
                }

                // Step 9: Check if data is ready to be read
                if (key.isReadable()) {
                    handleRead(key);
                }

                // Step 10: Check if it's ready to write data
                if (key.isWritable()) {
                    handleWrite(key);
                }

                // Remove the key to avoid re-processing
                iter.remove();
            }
        }
    }

    private static void handleAccept(SelectionKey key) throws IOException {
        // Step 11: Accept the connection and configure it as non-blocking
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);

        // Step 12: Register the client SocketChannel with the Selector for both reading and writing
        clientChannel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        System.out.println("Accepted connection from " + clientChannel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {
        // Step 13: Read data from the client
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            clientChannel.close();
        } else {
            var response = new ResponsePayload(buffer);
            System.out.println("Received message: " + response.getIP() + " with int " + response.getResponse());
        }
    }

    private static void handleWrite(SelectionKey key) throws IOException {
        // Step 14: Write data to the client
        SocketChannel clientChannel = (SocketChannel) key.channel();

        var payload = new RequestPayload("111.111.111.111", 10);

        clientChannel.write(payload.toByteBuffer().flip()); // TODO

        // Optionally, after writing, remove the OP_WRITE interest (if no further writing is needed)
        key.interestOps(SelectionKey.OP_READ);

        System.out.println("Sent message to client");
    }
}
