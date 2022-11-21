package github.hsien.rpc.core.registry.impl.zk;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.NetUtils;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceConsumer;
import github.hsien.rpc.core.registry.ServiceRegistry;
import github.hsien.rpc.core.registry.exception.ServiceConsumerException;
import github.hsien.rpc.core.registry.loadbalance.LoadBalance;
import github.hsien.rpc.core.registry.loadbalance.LoadBalanceType;

import java.net.SocketAddress;
import java.util.List;

/**
 * Zk Service Consumer
 *
 * @author hsien
 */
@LoadLevel(name = RegistryType.ZK)
public class ZkServiceConsumer implements ServiceConsumer {
    private final LoadBalance loadBalance;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceConsumer() {
        this.loadBalance = EnhancedServiceLoader.load(LoadBalance.class, LoadBalanceType.CONSISTENT_HASH);
        this.serviceRegistry = EnhancedServiceLoader.load(ServiceRegistry.class, RegistryType.ZK);
    }

    @Override
    public SocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        List<String> serviceUrlList = serviceRegistry.discoverServices(rpcServiceName);
        String serviceAddr = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        if (serviceAddr == null) {
            throw new ServiceConsumerException("Failed to find a available provider for service: " + rpcServiceName);
        }
        return NetUtils.parseSocketAddress(serviceAddr);
    }
}
