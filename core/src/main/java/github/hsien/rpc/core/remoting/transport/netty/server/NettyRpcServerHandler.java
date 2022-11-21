package github.hsien.rpc.core.remoting.transport.netty.server;

import github.hsien.rpc.common.util.RandomUtil;
import github.hsien.rpc.core.compress.CompressType;
import github.hsien.rpc.core.dto.RpcMessage;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.dto.RpcResponseCode;
import github.hsien.rpc.core.remoting.constants.RpcMessageConstants;
import github.hsien.rpc.core.remoting.exception.RpcException;
import github.hsien.rpc.core.remoting.exception.RpcServerException;
import github.hsien.rpc.core.remoting.transport.handler.RpcRequestHandler;
import github.hsien.rpc.core.serialize.SerializerType;
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
 * Customize the ChannelHandler of the server to process the data sent by the client.
 *
 * @author hsien
 */
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcServerHandler.class);
    private final RpcRequestHandler requestHandler;

    public NettyRpcServerHandler(RpcRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                LOGGER.info("Netty server received msg from [{}]: {}", ctx.channel().remoteAddress(), msg);
                RpcMessage<?> inMessage = (RpcMessage<?>) msg;
                byte messageType = inMessage.getMessageType();
                RpcMessage<?> outMessage;
                byte serializeCode = SerializerType.Code.KRYO_CODE.getCode();
                byte compressCode = CompressType.Code.GZIP_CODE.getCode();
                if (messageType == RpcMessageConstants.HEARTBEAT_REQUEST_TYPE) {
                    outMessage = RpcMessage.builder()
                        .messageType(RpcMessageConstants.HEARTBEAT_RESPONSE_TYPE)
                        .codec(serializeCode)
                        .compress(compressCode)
                        .messageId(RandomUtil.randomInt())
                        .data(RpcMessageConstants.PONG).build();
                } else if (messageType == RpcMessageConstants.REQUEST_TYPE) {
                    RpcRequest rpcRequest = (RpcRequest) inMessage.getData();
                    // invoke service here
                    Object result = requestHandler.handle(rpcRequest);
                    RpcResponse<?> response;
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        response = RpcResponse.success(result, rpcRequest.getRequestId());
                    } else {
                        response = RpcResponse.fail(RpcResponseCode.FAIL);
                    }
                    outMessage = RpcMessage.builder()
                        .messageType(RpcMessageConstants.RESPONSE_TYPE)
                        .codec(serializeCode)
                        .compress(compressCode)
                        .messageId(RandomUtil.randomInt())
                        .data(response).build();
                } else {
                    throw new RpcServerException("Unknown message type received: " + messageType);
                }
                ctx.writeAndFlush(outMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                LOGGER.info("Netty server inbound channel: no read operation was performed from [{}], "
                    + "and try to close", ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof RpcException) {
            LOGGER.error("Netty server inbound channel: caught rpc exception from [{}]",
                ctx.channel().remoteAddress());
        } else if (cause instanceof IOException) {
            LOGGER.error("Netty server inbound channel: caught IO exception from [{}], and try to close",
                ctx.channel().remoteAddress(), cause);
            ctx.channel().close();
        } else {
            LOGGER.error("Netty server inbound channel: caught error from [{}], and try to close",
                ctx.channel().remoteAddress(), cause);
            ctx.channel().close();
        }
    }
}
