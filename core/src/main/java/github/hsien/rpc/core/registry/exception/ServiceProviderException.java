package github.hsien.rpc.core.registry.exception;

/**
 * Service provider exception
 *
 * @author hsien
 */
public class ServiceProviderException extends RuntimeException {
    public ServiceProviderException(String msg) {
        super(msg);
    }

    public ServiceProviderException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServiceProviderException(Throwable cause) {
        super(cause);
    }
}
