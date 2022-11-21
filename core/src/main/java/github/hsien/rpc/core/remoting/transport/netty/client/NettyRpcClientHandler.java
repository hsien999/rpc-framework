package github.hsien.rpc.core.remoting.transport.netty.client;


import github.hsien.rpc.core.compress.CompressType;
import github.hsien.rpc.core.dto.RpcMessage;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.remoting.constants.RpcMessageConstants;
import github.hsien.rpc.core.remoting.exception.RpcException;
import github.hsien.rpc.core.remoting.exception.RpcServerException;
import github.hsien.rpc.core.serialize.SerializerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 *
 * @author hsien
 */
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcClient.class);
    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler(NettyRpcClient nettyRpcClient, UnprocessedRequests upRequests) {
        this.nettyRpcClient = nettyRpcClient;
        this.unprocessedRequests = upRequests;
    }

    /**
     * Read message from the channel pipeline
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage<?> message = (RpcMessage<?>) msg;
                byte messageType = message.getMessageType();
                if (messageType == RpcMessageConstants.HEARTBEAT_RESPONSE_TYPE) {
                    LOGGER.info("Netty client got heartbeat from [{}]: {}",
                        ctx.channel().remoteAddress(), message.getData());
                } else if (messageType == RpcMessageConstants.RESPONSE_TYPE) {
                    LOGGER.info("Netty client got response from [{}]: {}",
                        ctx.channel().remoteAddress(), message.getData());
                    RpcResponse<?> response = (RpcResponse<?>) message.getData();
                    unprocessedRequests.complete(response.getRequestId(), response);
                } else {
                    throw new RpcServerException("Unknown message type received: " + messageType);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Listen event from channel pipeline
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                LOGGER.info("Netty client inbound channel: no write operation was performed to [{}]"
                    + ", and try to send heartbeat request", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel(ctx.channel().remoteAddress());
                RpcMessage<?> rpcMessage = RpcMessage.builder()
                    .messageType(RpcMessageConstants.HEARTBEAT_REQUEST_TYPE)
                    .codec(SerializerType.Code.KRYO_CODE.getCode())
                    .compress(CompressType.Code.GZIP_CODE.getCode())
                    .data(RpcMessageConstants.PING).build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in the pipeline
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof RpcException) {
            LOGGER.error("Netty client inbound channel: caught rpc exception from {}",
                ctx.channel().remoteAddress());
        } else if (cause instanceof IOException) {
            LOGGER.error("Netty client inbound channel: caught IO exception from {}, and try to close",
                ctx.channel().remoteAddress(), cause);
            ctx.channel().close();
        } else {
            LOGGER.error("Netty client inbound channel: got error from [{}], and try to close",
                ctx.channel().remoteAddress(), cause);
            ctx.channel().close();
        }
    }
}

