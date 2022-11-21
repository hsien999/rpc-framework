package github.hsien.rpc.common.loader.exception;

/**
 * Initialize exception
 * This exception is thrown when an instance implementing
 * {@link github.hsien.rpc.common.loader.Initializable} throws something
 *
 * @author hsien
 */
public class InitializeException extends RuntimeException {
    public InitializeException(String msg) {
        super(msg);
    }

    public InitializeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
