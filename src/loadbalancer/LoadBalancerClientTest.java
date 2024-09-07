package loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class LoadBalancerClientTest {
    public static void main(String args[]) throws IOException {
        SocketChannel server = SocketChannel.open();
        server.configureBlocking(true);
        server.connect(new InetSocketAddress("localhost", 8000));
        System.out.println("Connected to server");

        ByteBuffer req = ByteBuffer.allocate(Integer.BYTES);
        req.putInt(Integer.parseInt(args[0]));
        req.flip();
        server.write(req);

        ByteBuffer response = ByteBuffer.allocate(1024);
        int bytesRead = server.read(response);
        response.flip();

        if (bytesRead == -1) {
            server.close();
        } else {
            ResponsePayload res = new ResponsePayload(response);
            System.out.println("Got response: " + res.getResponse());
        }
    }
}
