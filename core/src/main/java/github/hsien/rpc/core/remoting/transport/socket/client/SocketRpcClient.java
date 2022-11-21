package github.hsien.rpc.core.remoting.transport.socket.client;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.threadpool.ThreadPoolConfig;
import github.hsien.rpc.common.util.threadpool.ThreadPoolFactoryUtils;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceConsumer;
import github.hsien.rpc.core.remoting.RpcClient;
import github.hsien.rpc.core.remoting.RpcTransportType;
import github.hsien.rpc.core.remoting.exception.RpcClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Socket rpc client
 *
 * @author hsien
 */
@LoadLevel(name = RpcTransportType.SOCKET)
public class SocketRpcClient implements RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcClient.class);
    private static final ExecutorService WORKER_SERVICE = ThreadPoolFactoryUtils.createThreadPool(
        ThreadPoolConfig.builder().corePoolSize(5).build(), "socket-client-service", true);
    private final ServiceConsumer serviceConsumer;

    public SocketRpcClient() {
        this.serviceConsumer = EnhancedServiceLoader.load(ServiceConsumer.class, RegistryType.ZK);
    }

    @Override
    public void initialize() {
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            CompletableFuture<Object> result = new CompletableFuture<>();
            Socket socket = new Socket();
            WORKER_SERVICE.submit(() -> {
                SocketAddress address = serviceConsumer.lookupService(rpcRequest);
                try {
                    socket.connect(address);
                    // send data to the server via socket output stream
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(rpcRequest);
                    // receive data from server via socket input stream
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    result.complete(input.readObject());
                } catch (Exception e) {
                    throw new RpcClientException(e);
                }
            });
            RpcResponse<?> response = (RpcResponse<?>) result.get();
            LOGGER.info("Netty client got response from [{}]: {}", socket.getInetAddress(), response);
            socket.close();
            return response;
        } catch (Exception e) {
            throw new RpcClientException(e);
        }
    }
}
