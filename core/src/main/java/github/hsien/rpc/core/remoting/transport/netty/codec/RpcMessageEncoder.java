package github.hsien.rpc.core.remoting.transport.netty.codec;


import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.core.compress.Compress;
import github.hsien.rpc.core.compress.CompressType;
import github.hsien.rpc.core.dto.RpcMessage;
import github.hsien.rpc.core.remoting.constants.RpcMessageConstants;
import github.hsien.rpc.core.remoting.exception.RpcMessageEncodeException;
import github.hsien.rpc.core.serialize.Serializer;
import github.hsien.rpc.core.serialize.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * Custom protocol encoder
 * <pre>{@code
 *   0    ...    4         5     ...     9             10      11         12   ...   16
 *   +--  ...  --+---------+--   ...   --+-------------+-------+---------+-    ...   -+
 *   | magic code| version | full length | messageType | codec | compress | messageId |
 *   +--------------------------------------------------------------------------------+
 *   |                                                                                |
 *   |                                       body                                     |
 *   |                                      ... ...                                   |
 *   +--------------------------------------------------------------------------------+
 * }</pre>
 * <p> protocol header: 4B magic code, 1B version, 4B full length, 1B messageType, 1B codec, 1B compress, 4B messageId
 * <p> protocol body: object
 * <p> some constants used as default value, see {@link RpcMessageConstants}
 *
 * @author hsien
 * @see <a href="https://netty.io/4.0/api/io/netty/handler/codec/MessageToByteEncoder.html">
 * MessageToByteEncoder</a>
 * @see <a href="https://netty.io/4.0/api/io/netty/handler/codec/LengthFieldBasedFrameDecoder.html">
 * LengthFieldBasedFrameDecoder</a>
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage<?>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage<?> rpcMessage, ByteBuf out) {
        try {
            // write: magic number (4B)
            out.writeBytes(RpcMessageConstants.MAGIC_NUMBER);
            // write:version (1B)
            out.writeByte(RpcMessageConstants.VERSION);
            // skip: full length (4B)
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            // write: message type (1B)
            out.writeByte(messageType);
            // write: codec (1B)
            out.writeByte(rpcMessage.getCodec());
            // write: compress type (1B)
            out.writeByte(rpcMessage.getCompress());
            // write: message id (4B)
            out.writeInt(rpcMessage.getMessageId());
            int fullLength = RpcMessageConstants.HEAD_LENGTH;
            // serialize the object
            Serializer serializer = EnhancedServiceLoader.load(Serializer.class,
                SerializerType.Code.getName(rpcMessage.getCodec()));
            byte[] bodyBytes = serializer.serialize(rpcMessage.getData());
            // compress the bytes
            Compress compressor = EnhancedServiceLoader.load(Compress.class,
                CompressType.Code.getName(rpcMessage.getCompress()));
            bodyBytes = compressor.compress(bodyBytes);
            fullLength += bodyBytes.length;
            // write: body
            out.writeBytes(bodyBytes);
            int endIndex = out.writerIndex();
            // write: full length
            out.writerIndex(endIndex - fullLength + RpcMessageConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(endIndex);
        } catch (Exception e) {
            throw new RpcMessageEncodeException("Failed to encode message", e);
        }
    }
}

