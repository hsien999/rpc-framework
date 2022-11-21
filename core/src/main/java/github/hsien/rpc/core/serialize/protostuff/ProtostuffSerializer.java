package github.hsien.rpc.core.serialize.protostuff;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.serialize.SerializeException;
import github.hsien.rpc.core.serialize.Serializer;
import github.hsien.rpc.core.serialize.SerializerType;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.Serializable;

/**
 * Protostuff serializer
 *
 * @author hsien
 */
@LoadLevel(name = SerializerType.PROTOSTUFF)
public class ProtostuffSerializer implements Serializer {

    /**
     * Avoid reapplying buffer space each time you serialize
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> byte[] serialize(T obj) {
        try {
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(obj.getClass());
            byte[] bytes;
            try {
                bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
            } finally {
                BUFFER.clear();
            }
            return bytes;
        } catch (Exception e) {
            throw new SerializeException(SerializerType.PROTOSTUFF, "Failed to serialize", e);
        }
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T obj = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
            return obj;
        } catch (Exception e) {
            throw new SerializeException(SerializerType.PROTOSTUFF, "Failed to deserialize", e);
        }
    }
}
