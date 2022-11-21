package github.hsien.rpc.core.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.serialize.SerializeException;
import github.hsien.rpc.core.serialize.Serializer;
import github.hsien.rpc.core.serialize.SerializerType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Kryo serializer
 * <p>
 * Serialization with Kryo is efficient, but only compatible with the Java object transforming
 *
 * @author hsien
 */
@LoadLevel(name = SerializerType.KRYO)
public class KryoSerializer implements Serializer {
    /**
     * Kryo is not thread-safe, so use ThreadLocal to store Kryo objects
     */
    private final ThreadLocal<Kryo> kryoThreadLocal;

    public KryoSerializer() {
        try {
            this.kryoThreadLocal = ThreadLocal.withInitial(() -> {
                Kryo kryo = new Kryo();
                kryo.setRegistrationRequired(false);
                return kryo;
            });
        } catch (Exception e) {
            throw new SerializeException("Failed to initialize a kryo instance", e);
        }
    }

    @Override
    public <T extends Serializable> byte[] serialize(T obj) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Output kryoOutput = new Output(outputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(kryoOutput, obj);
            kryoThreadLocal.remove();
            return kryoOutput.toBytes();
        } catch (Exception e) {
            throw new SerializeException(SerializerType.KRYO, "Failed to serialize", e);
        }
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             Input kryoInput = new Input(inputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(kryoInput, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializeException(SerializerType.KRYO, "Failed to deserialize", e);
        }
    }
}
