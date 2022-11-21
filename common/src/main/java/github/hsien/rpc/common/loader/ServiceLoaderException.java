package github.hsien.rpc.common.loader;

/**
 * Service loader exception
 *
 * @author hsien
 */
public final class ServiceLoaderException extends RuntimeException {

    public ServiceLoaderException(String msg) {
        super(msg);
    }

    public ServiceLoaderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
