package github.hsien.rpc.core.registry.exception;

/**
 * Service register exception
 *
 * @author hsien
 */
public class ServiceRegisterException extends RuntimeException {
    public ServiceRegisterException(String msg) {
        super(msg);
    }

    public ServiceRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServiceRegisterException(Throwable cause) {
        super(cause);
    }
}
