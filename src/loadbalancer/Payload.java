package loadbalancer;

import java.nio.ByteBuffer;

public class Payload {
    private byte[] ip; // the first byte is the left-most IP value, IPv4
    private int time;

    // must be a non-flipped buffer
    public Payload(ByteBuffer buf) {
        ip = new byte[4];
        buf.flip();
        buf.get(ip, 0, 4);
        time = buf.getInt();
    }

    public Payload(String ipStr, int time)
    {
        String[] values = ipStr.split("\\.");
        ip = new byte[4];

        for (int i = 0; i < values.length; ++i) {
            ip[i] = (byte) Integer.parseInt(values[i]);
        }
        this.time = time;
    }

    public String getIP() {
        return Byte.toString(ip[0]) + "." +  Byte.toString(ip[1]) + "." +  Byte.toString(ip[2]) + "." +  Byte.toString(ip[3]);
    }

    public int getTime() {
        return time;
    }

    /**
     * 
     * @return a non-flipped ByteBuffer with capacity 8, and position at the end
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(ip);
        buf.putInt(time);
        return buf;
    }

    // public static void main(String[] args) {
    //     Payload p1 = new Payload("192.168.1.2", 35);

    //     assert(p1.getIP().equals("192.168.1.2"));
    //     assert(p1.getTime() == 35);

    //     ByteBuffer buf = p1.toByteBuffer();
    //     assert(buf.remaining() == 8);
    //     Payload p2 = new Payload(buf);

    //     assert(p2.getIP().equals("192.168.1.2"));
    //     assert(p2.getTime() == 35);
    // }
}
