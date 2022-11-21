package github.hsien.rpc.core.compress;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Compress/Decompress algorithm type
 *
 * @author hsien
 */
public class CompressType {
    public static final String GZIP = "Gzip";

    @AllArgsConstructor
    @Getter
    public enum Code {
        /**
         * GZIP compress
         */
        GZIP_CODE((byte) 0x01, GZIP);

        private final byte code;
        private final String name;

        public static String getName(byte code) {
            for (Code c : Code.values()) {
                if (c.getCode() == code) {
                    return c.getName();
                }
            }
            throw new IllegalArgumentException("Unrecognized code: " + code);
        }
    }
}
