package github.hsien.rpc.core.remoting.exception;

/**
 * Rpc client exception
 *
 * @author hsien
 */
public class RpcClientException extends RuntimeException {
    public RpcClientException(String msg) {
        super(msg);
    }

    public RpcClientException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcClientException(Throwable cause) {
        super(cause);
    }
}
