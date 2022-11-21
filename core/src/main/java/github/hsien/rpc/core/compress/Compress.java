package github.hsien.rpc.core.compress;


import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcMessage;

/**
 * Provide compress and decompress operations for body data in rpc message {@link RpcMessage}
 *
 * @author hsien
 */
@SPI
public interface Compress {
    /**
     * Compress byte array
     *
     * @param bytes original byte array
     * @return compressed byte array
     * @throws CompressException compress exception
     */
    byte[] compress(byte[] bytes);

    /**
     * Decompress byte array
     *
     * @param bytes original byte array
     * @return decompressed byte array
     * @throws CompressException compress exception
     */
    byte[] decompress(byte[] bytes);
}
