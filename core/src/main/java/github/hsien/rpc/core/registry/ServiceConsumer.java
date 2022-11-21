package github.hsien.rpc.core.registry;


import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.registry.exception.ServiceConsumerException;

import java.net.SocketAddress;


/**
 * Service consumer interface providing service lookup capabilities
 *
 * @author hsien
 */
@SPI
public interface ServiceConsumer {
    /**
     * lookup service by rpcServiceName
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     * @throws ServiceConsumerException service discovery exception
     */
    SocketAddress lookupService(RpcRequest rpcRequest);
}
