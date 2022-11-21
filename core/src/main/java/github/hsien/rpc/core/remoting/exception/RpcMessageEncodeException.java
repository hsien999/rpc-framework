package github.hsien.rpc.core.remoting.exception;

/**
 * Rpc message encode/decode exception
 *
 * @author hsien
 */
public class RpcMessageEncodeException extends RuntimeException {
    public RpcMessageEncodeException(String msg) {
        super(msg);
    }

    public RpcMessageEncodeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcMessageEncodeException(Throwable cause) {
        super(cause);
    }
}
