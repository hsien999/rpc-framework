package github.hsien.rpc.core.remoting.exception;

/**
 * Rpc server exception
 *
 * @author hsien
 */
public class RpcServerException extends RuntimeException {
    public RpcServerException(String msg) {
        super(msg);
    }

    public RpcServerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcServerException(Throwable cause) {
        super(cause);
    }
}
