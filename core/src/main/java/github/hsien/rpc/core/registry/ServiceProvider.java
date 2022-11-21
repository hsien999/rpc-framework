package github.hsien.rpc.core.registry;

import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.registry.exception.ServiceProviderException;

import java.net.SocketAddress;


/**
 * Provider interface supporting service publish, acquisition and cancellation functions
 *
 * @author hsien
 */
@SPI
public interface ServiceProvider {
    /**
     * Publish a rpc service
     *
     * @param rpcServiceConfig rpc service config
     * @param address          address of service provider
     * @throws ServiceProviderException service provider exception
     */
    void publishService(RpcServiceConfig rpcServiceConfig, SocketAddress address);

    /**
     * Get the rpc service object with specified service name
     *
     * @param rpcServiceName rpc service name
     * @return service object
     * @throws ServiceProviderException service provider exception
     */
    Object getService(String rpcServiceName);

    /**
     * Cancellation service with specified address
     *
     * @param address address of service provider
     * @throws ServiceProviderException service provider exception
     */
    void cancellationServices(SocketAddress address);
}
