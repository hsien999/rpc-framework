package github.hsien.rpc.core.registry.exception;

/**
 * Service consumer exception
 *
 * @author hsien
 */
public class ServiceConsumerException extends RuntimeException {
    public ServiceConsumerException(String msg) {
        super(msg);
    }

    public ServiceConsumerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServiceConsumerException(Throwable cause) {
        super(cause);
    }
}
