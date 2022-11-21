package github.hsien.rpc.core.remoting.transport.socket.server;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.NetUtils;
import github.hsien.rpc.common.util.threadpool.ThreadPoolConfig;
import github.hsien.rpc.common.util.threadpool.ThreadPoolFactoryUtils;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceProvider;
import github.hsien.rpc.core.remoting.RpcServer;
import github.hsien.rpc.core.remoting.RpcTransportType;
import github.hsien.rpc.core.remoting.transport.handler.RpcRequestHandler;
import github.hsien.rpc.core.remoting.transport.handler.RpcRequestHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * Socket rpc server
 *
 * @author hsien
 */
@LoadLevel(name = RpcTransportType.SOCKET)
public class SocketRpcServer implements RpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketRpcServer.class);
    private static final ExecutorService WORKER_SERVICE = ThreadPoolFactoryUtils.createThreadPool(
        ThreadPoolConfig.builder().corePoolSize(5).build(), "socket-server-service", true);

    private final SocketAddress socketAddress;
    private final ServiceProvider serviceProvider;
    private final RpcRequestHandler requestHandler;

    public SocketRpcServer() {
        int socketPort = 20220;
        this.socketAddress = new InetSocketAddress(NetUtils.getLocalHostExactAddress(), socketPort);
        this.serviceProvider = EnhancedServiceLoader.load(ServiceProvider.class, RegistryType.ZK);
        this.requestHandler = new RpcRequestHandlerImpl(serviceProvider);
    }

    @Override
    public void initialize() {
        WORKER_SERVICE.submit(this::bootstrap);
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        this.serviceProvider.publishService(rpcServiceConfig, socketAddress);
    }

    public void bootstrap() {
        LOGGER.info("Start to bootstrap socket server");
        try (ServerSocket server = new ServerSocket()) {
            server.bind(this.socketAddress);
            Socket socket;
            while ((socket = server.accept()) != null) {
                WORKER_SERVICE.execute(new RequestHandler(socket, requestHandler));
            }
            WORKER_SERVICE.shutdown();
        } catch (Exception e) {
            LOGGER.error("Socket server caught exception", e);
        }
    }

    private static final class RequestHandler implements Runnable {
        private final Socket socket;
        private final RpcRequestHandler requestHandler;

        private RequestHandler(Socket socket, RpcRequestHandler requestHandler) {
            this.socket = socket;
            this.requestHandler = requestHandler;
        }

        @Override
        public void run() {
            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                RpcRequest rpcRequest = (RpcRequest) input.readObject();
                LOGGER.info("Socket server received msg from [{}]: {}", rpcRequest, socket.getInetAddress());
                Object result = requestHandler.handle(rpcRequest);
                output.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            } catch (Exception e) {
                LOGGER.error("Socket server caught exception", e);
            } finally {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
