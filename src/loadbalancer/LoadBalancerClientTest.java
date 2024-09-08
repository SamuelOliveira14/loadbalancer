package loadbalancer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class VecWrapper {
    private Vector<Long> vec;
    public VecWrapper(int size) {
        vec = new Vector<>();
        for(int i = 0; i < size; ++i) {
            vec.add((long) 0);
        }
    }

    public synchronized void set(int i, long e) {
        vec.set(i,  e);
    }

    public long get(int i) {
        return vec.get(i);
    }
}

public class LoadBalancerClientTest {
    public static void main(String args[]) throws IOException {
        File file = new File("timeLoads.txt");
        Scanner myReader = new Scanner(file);
        var timeLoadVec = new Vector<Integer>();
        int numClients = 0;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            timeLoadVec.add(Integer.parseInt(data));
            numClients++;
        }
        myReader.close();
        System.out.println("Read " + numClients + "entries");

        ExecutorService threadPool = Executors.newFixedThreadPool(numClients);
        var timeFinishedVec = new VecWrapper(numClients);

        for (int i = 0; i < numClients; ++i) {
            final int index = i;
            threadPool.submit(() -> {
                try {
                    SocketChannel server = SocketChannel.open();
                    server.configureBlocking(true);
                    server.connect(new InetSocketAddress("localhost", 8000));
                    System.out.println("Connected to load balancer server");

                    ByteBuffer req = ByteBuffer.allocate(Integer.BYTES);
                    req.putInt(timeLoadVec.get(index));
                    req.flip();

                    ByteBuffer response = ByteBuffer.allocate(1024);

                    server.write(req);
                    long start = System.currentTimeMillis();
                    int bytesRead = server.read(response);
                    long end = System.currentTimeMillis();
                    System.out.println("Finished end " + end + ", start " + start + ", end - start" + (end - start));
                    timeFinishedVec.set(index, end - start);

                    response.flip();

                    if (bytesRead == -1) {
                        server.close();
                    } else {
                        ResponsePayload res = new ResponsePayload(response);
                        System.out.println("Got response: " + res.getResponse());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PrintWriter writer = new PrintWriter("outputRoundRobin.txt", "UTF-8");
        for (int i = 0; i < numClients; ++i) {
            writer.println(timeLoadVec.get(i) + " " + timeFinishedVec.get(i));
        }

        writer.close();
    }
}
