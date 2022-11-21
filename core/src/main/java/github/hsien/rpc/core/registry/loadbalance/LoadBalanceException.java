package github.hsien.rpc.core.registry.loadbalance;

/**
 * Load balance exception
 *
 * @author hsien
 */
public class LoadBalanceException extends RuntimeException {
    public LoadBalanceException(String msg) {
        super(msg);
    }

    public LoadBalanceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
