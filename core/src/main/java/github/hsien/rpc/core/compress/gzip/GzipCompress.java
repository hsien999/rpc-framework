package github.hsien.rpc.core.compress.gzip;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.compress.Compress;
import github.hsien.rpc.core.compress.CompressException;
import github.hsien.rpc.core.compress.CompressType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip compress
 *
 * @author hsien
 */
@LoadLevel(name = CompressType.GZIP)
public class GzipCompress implements Compress {
    /**
     * Default Buffer size: 2KB
     */
    private static final int BUFFER_SIZE = 2 << 10;

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (Exception e) {
            throw new CompressException(CompressType.GZIP, "Failed to compress", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CompressException(CompressType.GZIP, "Failed to decompress", e);
        }
    }
}
