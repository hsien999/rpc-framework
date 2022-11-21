package github.hsien.rpc.common.util;

/**
 * Runtime utils
 *
 * @author hsien
 */
public abstract class RuntimeUtils {
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
