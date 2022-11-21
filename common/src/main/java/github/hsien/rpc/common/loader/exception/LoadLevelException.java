package github.hsien.rpc.common.loader.exception;

/**
 * Load level exception
 *
 * @author hsien
 */
public class LoadLevelException extends RuntimeException {

    public LoadLevelException(String msg) {
        super(msg);
    }

    public LoadLevelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
