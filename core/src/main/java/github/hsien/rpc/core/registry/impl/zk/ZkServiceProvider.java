package github.hsien.rpc.core.registry.impl.zk;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.singleton.SingletonHolder;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceProvider;
import github.hsien.rpc.core.registry.ServiceRegistry;
import github.hsien.rpc.core.registry.exception.ServiceProviderException;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zk Service Provider
 *
 * @author hsien
 */
@LoadLevel(name = RegistryType.ZK)
public class ZkServiceProvider implements ServiceProvider {
    private final ConcurrentHashMap<String, SingletonHolder<Object>> serviceMap = new ConcurrentHashMap<>();
    private final ServiceRegistry serviceRegistry;


    public ZkServiceProvider() {
        this.serviceRegistry = EnhancedServiceLoader.load(ServiceRegistry.class, RegistryType.ZK);
    }

    @Override
    public void publishService(RpcServiceConfig config, SocketAddress address) {
        final String rpcServiceName = config.getRpcServiceName();
        System.out.println("rpcServiceName" + rpcServiceName);
        SingletonHolder<Object> holder = serviceMap.computeIfAbsent(rpcServiceName, k -> new SingletonHolder<>());
        holder.setIfAbsent(() -> {
            serviceRegistry.registerService(rpcServiceName, address);
            return config.getService();
        });
    }

    @Override
    public Object getService(String rpcServiceName) {
        SingletonHolder<Object> holder = serviceMap.get(rpcServiceName);
        if (holder == null || holder.get() == null) {
            throw new ServiceProviderException("Failed to find a service with given name: " + rpcServiceName);
        }
        return holder.get();
    }

    @Override
    public void cancellationServices(SocketAddress address) {
        this.serviceRegistry.cancellationServices(address);
    }
}
