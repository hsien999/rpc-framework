package github.hsien.rpc.core.registry.impl.zk;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.NetUtils;
import github.hsien.rpc.common.util.PropertiesUtils;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceRegistry;
import github.hsien.rpc.core.registry.exception.ServiceRegisterException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Zk Service Registry with Curator util
 *
 * @author hsien
 */
@LoadLevel(name = RegistryType.ZK)
public class ZkCuratorRegistry implements ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkCuratorRegistry.class);
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_SLEEP_TIME = 1000;
    private static final int DEFAULT_MAX_START_WAIT_TIME = 30;
    private static final String DEFAULT_CENTER_ADDRESS = "127.0.0.1:2181";
    private static final String ZK_REGISTER_ROOT_PATH = "/rpc-hsien";
    private static final String ZK_PATH_SEPARATOR = "/";
    private static final String ZK_PATH_SEPARATOR_REPLACE = "[\\]";
    private static final String RPC_CONFIG_PATH = "rpc.properties";
    private static final String ZK_ADDRESS_KEY = "rpc.zookeeper.address";

    private final ConcurrentHashMap<String, List<String>> servicesHolderMap = new ConcurrentHashMap<>();
    private final Set<String> registeredPathSet = ConcurrentHashMap.newKeySet();
    private final CuratorFramework client;

    public ZkCuratorRegistry() {
        Properties properties = PropertiesUtils.readProperties(RPC_CONFIG_PATH);
        String centerAddress = properties.getProperty(ZK_ADDRESS_KEY);
        if (centerAddress == null) {
            centerAddress = DEFAULT_CENTER_ADDRESS;
            LOGGER.warn("No available config found in {}, use default address [{}] instead", RPC_CONFIG_PATH,
                DEFAULT_CENTER_ADDRESS);
        }
        // retry policy: retry n times, and increase sleep time between retries
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(DEFAULT_SLEEP_TIME, DEFAULT_MAX_RETRIES);
        try {
            client = CuratorFrameworkFactory.builder().connectString(centerAddress).retryPolicy(retryPolicy).build();
            client.start();
            client.blockUntilConnected(DEFAULT_MAX_START_WAIT_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceRegisterException("Failed to start zk client via curator", e);
        }
    }

    @Override
    public void registerService(String rpcServiceName, SocketAddress address) {
        String path = encodeAndValidatePath(rpcServiceName, address);
        try {
            if (registeredPathSet.contains(path) || client.checkExists().forPath(path) != null) {
                LOGGER.info("Node is already registered in zk for service: {}", rpcServiceName);
            } else {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                registeredPathSet.add(path);
            }
        } catch (Exception e) {
            throw new ServiceRegisterException("Failed to create a node for service: " + rpcServiceName);
        }
    }

    @Override
    public List<String> discoverServices(String rpcServiceName) {
        if (servicesHolderMap.containsKey(rpcServiceName)) {
            return servicesHolderMap.get(rpcServiceName);
        } else {
            String servicePath = encodeAndValidatePath(rpcServiceName, null);
            try {
                List<String> result = client.getChildren().forPath(servicePath);
                servicesHolderMap.put(rpcServiceName, result);
                registerWatcher(rpcServiceName, client);
                for (int i = 0, size = result.size(); i < size; i++) {
                    result.set(i, decodeChildPath(result.get(i)));
                }
                return result;
            } catch (Exception e) {
                throw new ServiceRegisterException("Failed to get child nodes in zk for service: " + rpcServiceName, e);
            }
        }
    }

    @Override
    public void cancellationServices(SocketAddress address) {
        String addressName = NetUtils.socketAddressToString(address);
        registeredPathSet.forEach(path -> {
            try {
                if (path.endsWith(addressName)) {
                    client.delete().forPath(path);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to remove node in zk: " + path, e);
            }
        });
    }

    private String encodeAndValidatePath(String rpcServiceName, SocketAddress address) {
        String path = ZK_REGISTER_ROOT_PATH;
        if (rpcServiceName != null) {
            path += ZK_PATH_SEPARATOR + rpcServiceName.replace(ZK_PATH_SEPARATOR, ZK_PATH_SEPARATOR_REPLACE);
        }
        if (address != null) {
            path += ZK_PATH_SEPARATOR + NetUtils.socketAddressToString(address)
                .replace(ZK_PATH_SEPARATOR, ZK_PATH_SEPARATOR_REPLACE);
        }
        PathUtils.validatePath(path);
        return path;
    }

    private String decodeChildPath(String childPath) {
        return childPath.replace(ZK_PATH_SEPARATOR_REPLACE, ZK_PATH_SEPARATOR);
    }

    /**
     * Registry listener for changes of the specified service
     *
     * @param rpcServiceName rpc service name
     */
    private void registerWatcher(String rpcServiceName, CuratorFramework zkClient) {
        String servicePath = encodeAndValidatePath(rpcServiceName, null);
        try (CuratorCache cache = CuratorCache.builder(zkClient, servicePath).build()) {
            cache.listenable().addListener(CuratorCacheListener.builder()
                .forChanges(((oldNode, node) -> {
                    try {
                        servicesHolderMap.put(rpcServiceName, client.getChildren().forPath(servicePath));
                    } catch (Exception e) {
                        LOGGER.error("Failed to update cache service", e);
                    }
                })).build());
            cache.start();
        }
    }
}
