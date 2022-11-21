package github.hsien.rpc.core.remoting.transport.netty.codec;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.common.util.RandomUtil;
import github.hsien.rpc.core.compress.Compress;
import github.hsien.rpc.core.compress.CompressType;
import github.hsien.rpc.core.dto.RpcMessage;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.dto.RpcResponse;
import github.hsien.rpc.core.remoting.constants.RpcMessageConstants;
import github.hsien.rpc.core.remoting.exception.RpcMessageEncodeException;
import github.hsien.rpc.core.serialize.Serializer;
import github.hsien.rpc.core.serialize.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Custom protocol decoder
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
 * @see <a href="https://netty.io/4.0/api/io/netty/handler/codec/LengthFieldBasedFrameDecoder.html">
 * LengthFieldBasedFrameDecoder</a>
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * Creates a new decoder.
     * <p>lengthFieldOffset = 5, magic code (4B) + version (1B)
     * <p>lengthFieldLength = 4, full length (4B)
     * <p>lengthAdjustment = -9, offset of data after full length (full length - 9)
     * <p>initialBytesToStrip: 0, magic code and version will be checked manually, so do not strip any bytes
     */
    public RpcMessageDecoder() {
        this(RpcMessageConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * Creates a new decoder.
     *
     * @param maxFrameLength      the maximum length of the frame.  If the length of the frame is
     *                            greater than this value, {@link TooLongFrameException} will be
     *                            thrown.
     * @param lengthFieldOffset   the offset of the length field
     * @param lengthFieldLength   the length of the length field
     * @param lengthAdjustment    the compensation value to add to the value of the length field
     * @param initialBytesToStrip the number of first bytes to strip out from the decoded frame
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            Object decoded = super.decode(ctx, in);
            if (decoded instanceof ByteBuf) {
                ByteBuf frame = (ByteBuf) decoded;
                if (frame.readableBytes() >= RpcMessageConstants.TOTAL_LENGTH) {
                    try {
                        return decodeFrame(frame);
                    } finally {
                        frame.release();
                    }
                }
            }
            return decoded;
        } catch (Exception e) {
            throw new RpcMessageEncodeException("Failed to decode message from " + ctx.channel().remoteAddress(), e);
        }
    }

    private Object decodeFrame(ByteBuf in) {
        // read & check: magic code (4B) and version (1B)
        checkMagicNumber(in);
        checkVersion(in);
        // read: full length (4B)
        int fullLength = in.readInt();
        // read: message type (1B)
        byte messageType = in.readByte();
        // read: codec (1B)
        byte codecType = in.readByte();
        // read: compress type (1B)
        byte compressType = in.readByte();
        // read: message id (4B)
        in.readInt();
        Serializable data = null;
        int bodyLength = fullLength - RpcMessageConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            // TODO use ByteOutputStream
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // decompress the bytes
            Compress compressor = EnhancedServiceLoader.load(Compress.class,
                CompressType.Code.getName(compressType));
            bs = compressor.decompress(bs);
            // deserialize the object
            Serializer serializer = EnhancedServiceLoader.load(Serializer.class,
                SerializerType.Code.getName(codecType));
            if (messageType == RpcMessageConstants.REQUEST_TYPE) {
                data = serializer.deserialize(bs, RpcRequest.class);
            } else if (messageType == RpcMessageConstants.RESPONSE_TYPE) {
                data = serializer.deserialize(bs, RpcResponse.class);
            } else {
                data = serializer.deserialize(bs, String.class);
            }
        }
        return RpcMessage.builder().messageType(messageType).compress(compressType).codec(codecType)
            .messageId(RandomUtil.randomInt()).data(data).build();
    }

    private void checkMagicNumber(ByteBuf in) {
        int len = RpcMessageConstants.MAGIC_NUMBER.length;
        byte[] number = new byte[len];
        in.readBytes(number);
        for (int i = 0; i < len; i++) {
            if (number[i] != RpcMessageConstants.MAGIC_NUMBER[i]) {
                throw new IllegalStateException("Unknown magic code: " + Arrays.toString(number));
            }
        }
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcMessageConstants.VERSION) {
            throw new IllegalStateException("Protocol version isn't compatible: " + version);
        }
    }
}
