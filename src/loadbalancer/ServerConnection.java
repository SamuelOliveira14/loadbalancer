package loadbalancer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class ServerConnection {
    
    private String address;
    private int port;


    public ServerConnection(int port, String address) {
        this.port = port;
        this.address = address;
    }

    private Socket getSocket(){
        Socket socket = null;
        try {
            socket = new Socket(this.address, this.port);
        } catch (Exception e) {
            //handle exception
        }
        return socket;
    }

    public byte[] request(byte[] requestData){
        var connection = this.getSocket();

        byte[] response = null;

        try{
            BufferedOutputStream connectionOutput = new BufferedOutputStream(connection.getOutputStream());

            connectionOutput.write(requestData);
            connectionOutput.flush();

            BufferedInputStream connectionInput = new BufferedInputStream(connection.getInputStream());
            //handle response...

            connectionInput.close();
            connectionOutput.close();
            connection.close();

            
        }catch(Exception e){
            // handle
        }

        return response;
    }
    
}
