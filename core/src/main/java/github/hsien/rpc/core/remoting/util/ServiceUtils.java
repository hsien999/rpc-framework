package github.hsien.rpc.core.remoting.util;

/**
 * @author hsien
 */
public class ServiceUtils {
    private static final String SEPARATOR = "#";

    public static String getRpcServiceName(String interfaceName, String group, String version) {
        StringBuilder nameBuilder = new StringBuilder();
        appendName(nameBuilder, interfaceName, true);
        appendName(nameBuilder, group, false);
        appendName(nameBuilder, version, false);
        return nameBuilder.toString();
    }

    private static void appendName(StringBuilder appender, String name, boolean first) {
        if (name != null) {
            if (!first) {
                appender.append(SEPARATOR);
            }
            appender.append(name);
        }
    }
}
