package github.hsien.rpc.core.registry;

import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.registry.exception.ServiceRegisterException;

import java.net.SocketAddress;
import java.util.List;

/**
 * Service registry interface supporting service registration, discovery and cancellation functions
 *
 * @author hsien
 */
@SPI
public interface ServiceRegistry {
    /**
     * Register a rpc service
     *
     * @param rpcServiceName rpc service name
     * @param address        address of service provider
     * @throws ServiceRegisterException service register exception
     */
    void registerService(String rpcServiceName, SocketAddress address);

    /**
     * Discover the list of addresses of available providers for the specified service name
     *
     * @param rpcServiceName rpc service name
     * @return list containing addresses of available provider
     * @throws ServiceRegisterException service register exception
     */
    List<String> discoverServices(String rpcServiceName);

    /**
     * Cancellation service with specified address
     *
     * @param address address of service provider
     * @throws ServiceRegisterException service register exception
     */
    void cancellationServices(SocketAddress address);
}
