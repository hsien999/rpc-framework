package github.hsien.rpc.core.remoting;


import github.hsien.rpc.common.loader.Initializable;
import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.registry.exception.ServiceProviderException;

/**
 * @author hsien
 */
@SPI
public interface RpcServer extends Initializable {
    /**
     * Publish a rpc service
     *
     * @param rpcServiceConfig rpc service config
     * @throws ServiceProviderException service provider exception
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
