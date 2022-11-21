package github.hsien.rpc.core.remoting.transport.netty.client;

import github.hsien.rpc.core.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold unprocessed results in client site
 *
 * @author hsien
 */
public class UnprocessedRequests {
    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse<?>>>
        UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    /**
     * Associate the requestId with a completable future of response
     *
     * @param requestId the request id
     * @param rpcFuture completable rpc future
     */
    public void put(String requestId, CompletableFuture<RpcResponse<?>> rpcFuture) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, rpcFuture);
    }

    /**
     * Complete the request result
     *
     * @param requestId   request id
     * @param rpcResponse rpc response
     */
    public void complete(String requestId, RpcResponse<?> rpcResponse) {
        CompletableFuture<RpcResponse<?>> future = UNPROCESSED_RESPONSE_FUTURES.remove(requestId);
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
