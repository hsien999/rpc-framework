package github.hsien.rpc.core.remoting.constants;

/**
 * Some useful constants for rpc message
 *
 * @author hsien
 */
public class RpcMessageConstants {
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final byte VERSION = 0x01;
    public static final byte TOTAL_LENGTH = 0x16;
    public static final byte REQUEST_TYPE = 0x01;
    public static final byte RESPONSE_TYPE = 0x02;
    public static final byte HEARTBEAT_REQUEST_TYPE = 0x03;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 0x04;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final int MAX_FRAME_LENGTH = 10 << 20;
}
