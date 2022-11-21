package github.hsien.rpc.core.remoting.transport.netty.server;


import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.NetUtils;
import github.hsien.rpc.common.util.RuntimeUtils;
import github.hsien.rpc.common.util.threadpool.ThreadPoolConfig;
import github.hsien.rpc.common.util.threadpool.ThreadPoolFactoryUtils;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceProvider;
import github.hsien.rpc.core.remoting.RpcServer;
import github.hsien.rpc.core.remoting.RpcTransportType;
import github.hsien.rpc.core.remoting.exception.RpcServerException;
import github.hsien.rpc.core.remoting.transport.handler.RpcRequestHandlerImpl;
import github.hsien.rpc.core.remoting.transport.netty.codec.RpcMessageDecoder;
import github.hsien.rpc.core.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server. Receive the client message, call the corresponding method according to the client message,
 * and then return the result to the client.
 *
 * @author hsien
 */
@LoadLevel(name = RpcTransportType.NETTY)
public class NettyRpcServer implements RpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcServer.class);
    private static final ExecutorService WORKER_SERVICE = ThreadPoolFactoryUtils.createThreadPool(
        ThreadPoolConfig.builder().corePoolSize(1).build(), "netty-worker-service", true);

    private final SocketAddress socketAddress;
    private final ServiceProvider serviceProvider;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup serviceHandlerGroup;

    public NettyRpcServer() {
        // TODO specify by config
        int socketPort = 20220;
        this.socketAddress = new InetSocketAddress(NetUtils.getLocalHostExactAddress(), socketPort);
        this.serviceProvider = EnhancedServiceLoader.load(ServiceProvider.class, RegistryType.ZK);
    }

    /**
     * Initialize boss group, worker group, service handler group and bootstrap
     */
    @Override
    public void initialize() {
        if (isShutDown()) {
            LOGGER.warn("Duplicate initialization for netty netty server, nothing to be done");
            return;
        }
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtils.cpus() * 2,
            ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false));
        WORKER_SERVICE.submit(this::bootstrap);
    }

    /**
     * Publish service
     *
     * @param rpcServiceConfig rpc service config
     */
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig, this.socketAddress);
    }

    /**
     * Bootstrap group and listen to channels in a new worker
     */
    private void bootstrap() {
        LOGGER.info("Start to bootstrap netty server");
        try {
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // The TCP_NODELAY parameter is used to control whether the Nagle algorithm is enabled.
                .childOption(ChannelOption.TCP_NODELAY, true)
                // enable the TCP underlying heartbeat mechanism
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // SO_BACKLOG indicates the maximum length of the queue used by the system
                // to temporarily store requests that have completed three handshakes,
                // if the connection is established frequently and the server is slow
                // in processing the creation of new connections, this parameter can be appropriately adjusted upwards
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // Close the connection if no client request is received within 30 seconds
                        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        // p.addLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(serviceHandlerGroup, new NettyRpcServerHandler(
                            new RpcRequestHandlerImpl(serviceProvider)));
                    }
                });
            // Bind the port, synchronize and wait for successful binding
            ChannelFuture channelFuture = bootstrap.bind(socketAddress).sync();
            // Wait for the server-side listening port to close
            channelFuture.channel().closeFuture().sync();
            this.serviceProvider.cancellationServices(socketAddress);
        } catch (Exception e) {
            throw new RpcServerException(e);
        } finally {
            LOGGER.info("Netty server is shutting down");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }

    private boolean isShutDown() {
        return bossGroup != null && (bossGroup.isShutdown() || bossGroup.isShuttingDown())
            || workerGroup != null && (workerGroup.isShutdown() || workerGroup.isShuttingDown())
            || serviceHandlerGroup != null && (serviceHandlerGroup.isShutdown() || serviceHandlerGroup.isShuttingDown());
    }
}
