package github.hsien.rpc.core.remoting.exception;

/**
 * Common rpc exception
 *
 * @author hsien
 */
public class RpcException extends RuntimeException {
    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
