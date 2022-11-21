package github.hsien.rpc.core.remoting.transport.handler;

import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.registry.ServiceProvider;
import github.hsien.rpc.core.remoting.exception.RpcServerException;
import lombok.NonNull;

import java.lang.reflect.Method;

/**
 * A common implement of rpc request handler
 *
 * @author hsien
 */
public class RpcRequestHandlerImpl implements RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandlerImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Object handle(@NonNull RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, @NonNull Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (Exception e) {
            throw new RpcServerException("Failed to invoke the service [" + service + "] for request ["
                + rpcRequest + "]", e);
        }
        return result;
    }
}
