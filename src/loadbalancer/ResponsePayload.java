package loadbalancer;

import java.nio.ByteBuffer;

public class ResponsePayload {
    private byte[] ip; // the first byte is the left-most IP value, IPv4
    private int response;

    // must be a flipped buffer
    public ResponsePayload(ByteBuffer buf) {
        ip = new byte[4];
        buf.get(ip, 0, 4);
        response = buf.getInt();
    }

    public ResponsePayload(String ipStr, int response)
    {
        String[] values = ipStr.split("\\.");
        ip = new byte[4];

        for (int i = 0; i < values.length; ++i) {
            ip[i] = (byte) Integer.parseInt(values[i]);
        }
        this.response = response;
    }

    public String getIP() {
        return Byte.toString(ip[0]) + "." +  Byte.toString(ip[1]) + "." +  Byte.toString(ip[2]) + "." +  Byte.toString(ip[3]);
    }

    public int getResponse() {
        return response;
    }

    /**
     * 
     * @return a non-flipped ByteBuffer with capacity 8, and position at the end
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(ip);
        buf.putInt(response);
        return buf;
    }
}
