package github.hsien.rpc.core.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author hsien
 */
public class SerializerType {
    public static final String KRYO = "Kryo";
    public static final String PROTOSTUFF = "Protostuff";
    public static final String HESSIAN = "Hessian";

    @AllArgsConstructor
    @Getter
    public enum Code {
        /**
         * Kryo Serialization
         */
        KRYO_CODE((byte) 0x01, KRYO),
        /**
         * Protostuff Serialization
         */
        PROTOSTUFF_CODE((byte) 0x02, PROTOSTUFF),
        /**
         * Hessian Serialization
         */
        HESSIAN_CODE((byte) 0X03, HESSIAN);
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
