package loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WorkClientTest {
    public static void main(String[] args) throws IOException {
        SocketChannel server = SocketChannel.open();
        server.configureBlocking(true);
        server.connect(new InetSocketAddress("localhost", WorkServer.PORT));
        System.out.println("Connected to server");

        RequestPayload req = new RequestPayload("111.111.111.111", 5);
        server.write(req.toByteBuffer());

        ByteBuffer response = ByteBuffer.allocate(1024);
        int bytesRead = server.read(response);
        response.flip();

        if (bytesRead == -1) {
            server.close();
        }
        else {
            ResponsePayload res = new ResponsePayload(response);
            System.out.println("Got response: " + res.getResponse());
        }
    }
}
