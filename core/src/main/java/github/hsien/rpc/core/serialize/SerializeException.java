package github.hsien.rpc.core.serialize;

/**
 * Serialize exception
 *
 * @author hsien
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String msg) {
        super(msg);
    }

    public SerializeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException(String type, String msg, Throwable cause) {
        super(type + ": " + msg, cause);
    }
}
