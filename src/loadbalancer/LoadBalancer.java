package loadbalancer;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class LoadBalancer {
    private DistributionAlgorithm algorithm;
    private ServerSocket serverSocket;
    private List<ServerConnection> servers;

    
    public LoadBalancer(int port, String address, int maxQueue, DistributionAlgorithm algorithm) {
        
        this.algorithm = algorithm;
        this.servers = new ArrayList<ServerConnection>();
        this.serverSocket = createServerSocket(port, address, maxQueue);
    }
    
    public LoadBalancer(int port, String address, int maxQueue, DistributionAlgorithm algorithm, List<ServerConnection> servers) {
        
        this.algorithm = algorithm;
        this.servers = servers;
        this.serverSocket = createServerSocket(port, address, maxQueue);

    }

    private ServerSocket createServerSocket(int port, String address, int maxQueue) {
        
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, maxQueue, (Inet4Address) Inet4Address.getByName(address));
            serverSocket.setReuseAddress(true);
        } catch (Exception e) {
            //handle exception
        }

        return serverSocket;

    }

    public void start() {
        while(true) {
            if (this.servers.isEmpty()) {
                return;
            }
            try{
                Socket client = serverSocket.accept();
                ServerConnection connection = algorithm.nextServer();

                //thread pool can be a better idea (https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html)
                new Thread(
                    () -> {
                        try{

                            BufferedInputStream clientInput = new BufferedInputStream(client.getInputStream());
                            var response = connection.request(clientInput.readAllBytes()); //Don't know if this is the best way to read (prob not)...

                            BufferedOutputStream clientOutput = new BufferedOutputStream(client.getOutputStream()); //Buffer to write into client socket (bytes)
                            // handle response...

                            clientInput.close();
                            clientOutput.close();
                            client.close();

                        }catch(Exception e){
                            //handle exceptions...
                        } 
                    }
                ).start();

            }catch(Exception e) {

            }
        }

    }

    public void addServer(ServerConnection server) {
        this.servers.add(server);
        // Handle how DistributionAlgorithm works...
    }

    public void addServers(List<ServerConnection> servers) {
        this.servers.addAll(servers);
        // Handle how DistributionAlgorithm works...
    }

    @Override
    protected void finalize() {
        try {
            // close resources...
        } catch (Exception e) {

        }
    }

}
