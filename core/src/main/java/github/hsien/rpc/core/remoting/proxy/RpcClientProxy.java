package github.hsien.rpc.core.remoting.proxy;

import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.remoting.RpcClient;
import github.hsien.rpc.core.remoting.exception.RpcException;
import github.hsien.rpc.core.remoting.transport.netty.client.NettyRpcClient;
import github.hsien.rpc.core.remoting.transport.socket.client.SocketRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Rpc client proxy by implementing the {@link InvocationHandler} interface
 * <p>
 * When a dynamic proxy object calls a method, it actually calls the calling method below. Thanks to dynamic proxies,
 * the remote method is called by the client as if it were a local method (the intermediate process is blocked)
 * </p>
 *
 * @author hsien
 */
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient rpcClient;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    /**
     * Construct a new proxy instance based on an interface with this rpc client handler as an invocation handler.
     *
     * @param aInterface the interface for the proxy class to implement
     * @param <T>        type of proxy class
     * @return a proxy instance with this handler of a proxy class that is defined by the specified interface
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> aInterface) {
        return (T) Proxy.newProxyInstance(aInterface.getClassLoader(), new Class<?>[]{aInterface}, this);
    }

    /**
     * Processes a method invocation on the rpc service proxy instance and returns the result.
     * This method will be invoked on rpc client invocation handler when a method is invoked
     * on a rpc service proxy instance that it is associated with.
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return result of service call by rpc client
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .parameters(args)
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion()).build();
            RpcResponse<Object> rpcResponse = null;
            if (rpcClient instanceof NettyRpcClient) {
                CompletableFuture<RpcResponse<Object>> completableFuture =
                    (CompletableFuture<RpcResponse<Object>>) rpcClient.sendRpcRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            } else if (rpcClient instanceof SocketRpcClient) {
                rpcResponse = (RpcResponse<Object>) rpcClient.sendRpcRequest(rpcRequest);
            }
            this.check(rpcResponse, rpcRequest);
            return rpcResponse.getData();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    /**
     * Verify the consistency of request and response, and whether the response was successful.
     *
     * @param rpcResponse rpc response
     * @param rpcRequest  rpc request
     */
    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException("Empty response for service: " + rpcRequest.getRpcServiceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException("Incompatible requests and responses id for service: "
                + rpcRequest.getInterfaceName());
        }
    }
}
