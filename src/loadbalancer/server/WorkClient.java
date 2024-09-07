package loadbalancer.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import loadbalancer.ResquestPayload;

public class WorkClient {
    private SocketChannel socketChannel;

    public WorkClient(String addr, int port) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            // Initiate connection to the server
            InetSocketAddress server = new InetSocketAddress(addr, port);
            socketChannel.connect(server);

            // Wait until the connection is completed
            while (!socketChannel.finishConnect()) {
                System.out.println("Connecting to server...");
            }
            System.out.println("Connected to server!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            try {
                int bytesRead = socketChannel.read(buffer);

                // If bytesRead == -1, the server has closed the connection
                if (bytesRead == -1) {
                    System.out.println("Server has closed the connection.");
                    break;
                }

                if (bytesRead > 0) {
                    buffer.flip();
                    ResquestPayload rPayload = new ResquestPayload(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
