package github.hsien.rpc.core.remoting.transport.netty.client;


import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.common.util.RandomUtil;
import github.hsien.rpc.common.util.threadpool.ThreadPoolConfig;
import github.hsien.rpc.common.util.threadpool.ThreadPoolFactoryUtils;
import github.hsien.rpc.core.compress.CompressType;
import github.hsien.rpc.core.dto.RpcMessage;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.registry.RegistryType;
import github.hsien.rpc.core.registry.ServiceConsumer;
import github.hsien.rpc.core.remoting.RpcClient;
import github.hsien.rpc.core.remoting.RpcTransportType;
import github.hsien.rpc.core.remoting.constants.RpcMessageConstants;
import github.hsien.rpc.core.remoting.exception.RpcClientException;
import github.hsien.rpc.core.remoting.transport.netty.codec.RpcMessageDecoder;
import github.hsien.rpc.core.remoting.transport.netty.codec.RpcMessageEncoder;
import github.hsien.rpc.core.serialize.SerializerType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Netty rpc client
 *
 * @author hsien
 */
@LoadLevel(name = RpcTransportType.NETTY)
public class NettyRpcClient implements RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcClient.class);
    private static final ExecutorService WORKER_SERVICE = ThreadPoolFactoryUtils.createThreadPool(
        ThreadPoolConfig.builder().corePoolSize(1).build(), "netty-client-service", false);

    private final ServiceConsumer serviceConsumer;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        this.serviceConsumer = EnhancedServiceLoader.load(ServiceConsumer.class, RegistryType.ZK);
        this.unprocessedRequests = new UnprocessedRequests();
        this.channelProvider = new ChannelProvider();
    }

    @Override
    public void initialize() {
        if (isShutDown()) {
            LOGGER.warn("Duplicate initialization for netty netty server, nothing to be done");
            return;
        }
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(1);
        WORKER_SERVICE.submit(this::bootstrap);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            CompletableFuture<RpcResponse<?>> resultFuture = new CompletableFuture<>();
            Channel channel = getChannel(serviceConsumer.lookupService(rpcRequest));
            if (channel.isActive()) {
                unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
                RpcMessage<?> rpcMessage = RpcMessage.builder()
                    .messageType(RpcMessageConstants.REQUEST_TYPE)
                    .codec(SerializerType.Code.KRYO_CODE.getCode())
                    .compress(CompressType.Code.GZIP_CODE.getCode())
                    .messageId(RandomUtil.randomInt())
                    .data(rpcRequest).build();
                channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        LOGGER.info("Netty client send message: [{}]", rpcMessage);
                    } else {
                        future.channel().close();
                        resultFuture.completeExceptionally(future.cause());
                    }
                });
            } else {
                throw new IllegalStateException("Channel of netty client is inactive or closed");
            }
            return resultFuture;
        } catch (Exception e) {
            throw new RpcClientException("Error occurred while requesting", e);
        }
    }

    Channel getChannel(SocketAddress address) throws ExecutionException, InterruptedException {
        Channel channel = channelProvider.get(address);
        if (channel == null) {
            CompletableFuture<Channel> channelFeature = new CompletableFuture<>();
            bootstrap.connect(address).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    LOGGER.info("Netty client connected to server: {}", address);
                    channelFeature.complete(future.channel());
                } else {
                    LOGGER.error("Netty client failed to connect to server: {}", address);
                    throw new IllegalStateException("Failed to connect target address: " + address);
                }
            });
            channel = channelFeature.get();
            channelProvider.set(address, channel);
        }
        return channel;
    }

    private void bootstrap() {
        LOGGER.info("Start to bootstrap netty client");
        try {
            final NettyRpcClient rpcClient = this;
            bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
                        // p.addLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler(rpcClient, unprocessedRequests));
                    }
                });
            LOGGER.info("Netty client startup completed");
        } catch (Exception e) {
            throw new RpcClientException(e);
        }
    }

    private boolean isShutDown() {
        return eventLoopGroup != null && (eventLoopGroup.isShutdown() || eventLoopGroup.isShuttingDown());
    }
}
