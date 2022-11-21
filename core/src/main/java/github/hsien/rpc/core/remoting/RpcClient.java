package github.hsien.rpc.core.remoting;

import github.hsien.rpc.common.loader.Initializable;
import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.remoting.exception.RpcClientException;

/**
 * @author hsien
 */
@SPI
public interface RpcClient extends Initializable {
    /**
     * send rpc request and receive response data
     *
     * @param rpcRequest request body
     * @return response data
     * @throws RpcClientException rpc client exception
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
