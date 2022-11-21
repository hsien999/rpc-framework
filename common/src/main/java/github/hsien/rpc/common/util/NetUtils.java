package github.hsien.rpc.common.util;

import lombok.NonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Net utils
 *
 * @author hsien
 */
public abstract class NetUtils {
    private static final String HOST_PORT_SEPARATOR = ":";
    private static final String HOST_ADDRESS_SEPARATOR = "/";

    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddr = addresses.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static SocketAddress parseSocketAddress(@NonNull String addressStr) {
        int portIdx = addressStr.lastIndexOf(HOST_PORT_SEPARATOR);
        String hostAddress = addressStr.substring(0, portIdx);
        if (hostAddress.startsWith(HOST_ADDRESS_SEPARATOR)) {
            // without hostname prefix, e.g. /172.100.100.1
            hostAddress = hostAddress.substring(1);
        }
        try {
            InetAddress host = InetAddress.getByName(hostAddress);
            int port = Integer.parseInt(addressStr.substring(portIdx + 1));
            return new InetSocketAddress(host, port);
        } catch (UnknownHostException | NumberFormatException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String socketAddressToString(@NonNull SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            String addressStr = address.toString();
            return addressStr.startsWith(HOST_ADDRESS_SEPARATOR) ? addressStr.substring(1) : addressStr;
        }
        return address.toString();
    }
}
