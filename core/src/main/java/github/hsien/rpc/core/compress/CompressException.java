package github.hsien.rpc.core.compress;

/**
 * Compress/Decompress exception
 *
 * @author hsien
 */
public class CompressException extends RuntimeException {
    public CompressException(String msg) {
        super(msg);
    }

    public CompressException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CompressException(String type, String msg, Throwable cause) {
        super(type + ": " + msg, cause);
    }
}
