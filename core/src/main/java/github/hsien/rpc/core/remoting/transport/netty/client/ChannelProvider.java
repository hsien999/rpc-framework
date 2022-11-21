package github.hsien.rpc.core.remoting.transport.netty.client;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Hold channels in client site
 *
 * @author hsien
 */
public class ChannelProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelProvider.class);
    private final ConcurrentHashMap<SocketAddress, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * Get the available cache channel
     *
     * @param address remote address in channel
     * @return channel
     */
    public Channel get(SocketAddress address) {
        Channel channel = channelMap.get(address);
        if (channel != null && !channel.isActive()) {
            // do not lock, do nothing if not exists
            LOGGER.info("Channel [{}] is inactive and will be remove from cache", channel);
            channelMap.remove(address);
            return null;
        }
        return channel;
    }

    /**
     * Cache a channel with specified address
     *
     * @param address remote address in channel
     * @param channel channel
     */
    public void set(SocketAddress address, Channel channel) {
        channelMap.put(address, channel);
    }
}
