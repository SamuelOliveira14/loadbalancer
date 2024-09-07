package loadbalancer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

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
                    RequestPayload rPayload = new RequestPayload(buffer);

                    var response = processRequest(rPayload).toByteBuffer();
                    while (response.hasRemaining())
                        socketChannel.write(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ResponsePayload processRequest(RequestPayload p) {
        System.out.println(" Processing request " + p.getTime());
        int time = p.getTime();
        int returnVal = 0;
        try {
            TimeUnit.SECONDS.sleep(time);
            returnVal = 2 * time;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ResponsePayload(p.getIP(), 2 * returnVal);
    }

    public static void main(String args[]) {
        try {
            InetAddress host = InetAddress.getLocalHost();
            int port = 9876;

            var work = new WorkClient(host.getHostAddress(), port);
            work.run();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
