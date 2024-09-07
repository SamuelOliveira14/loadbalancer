package loadbalancer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ServerConnection {
    
    private String address;
    private int port;

    public ServerConnection(int port, String address) {
        this.port = port;
        this.address = address;
    }

    private SocketChannel getSocket(){
        SocketChannel socket = null;
        try {
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress(this.address, this.port));
            while(!socket.finishConnect()) continue;

            socket.configureBlocking(true);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return socket;
    }


    /**
     * 
     * @param payload
     * @return Flipped ByteBuffer with response from server
     */
    public ByteBuffer request(Payload payload) {

        var connectionChannel = this.getSocket();
        var requestBuffer = payload.toByteBuffer().flip();
        
        try{
            while (requestBuffer.hasRemaining()) connectionChannel.write(requestBuffer);

            ByteBuffer responseBuffer = ByteBuffer.allocate(8).clear(); // Check response ByteBuffer capacity with server later

            var bytesRead = connectionChannel.read(responseBuffer);
            if(bytesRead == -1){
                return null; // TODO: return failure code
            }

            connectionChannel.close();
            return responseBuffer.flip();

        }catch(Exception e){
            throw new RuntimeException();
        }
    }
    
}
