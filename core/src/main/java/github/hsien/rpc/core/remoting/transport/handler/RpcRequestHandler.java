package github.hsien.rpc.core.remoting.transport.handler;

import github.hsien.rpc.core.dto.RpcRequest;

/**
 * Rpc request handler
 *
 * @author hsien
 */
public interface RpcRequestHandler {
    /**
     * Define how to handle request in server site
     *
     * @param rpcRequest rpc request accepted from server
     * @return completable future of rpc response or result instance
     */
    Object handle(RpcRequest rpcRequest);
}
