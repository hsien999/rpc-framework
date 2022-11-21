package github.hsien.rpc.core.serialize.hessian;


import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.serialize.SerializeException;
import github.hsien.rpc.core.serialize.Serializer;
import github.hsien.rpc.core.serialize.SerializerType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Hessian serializer
 * <p>
 * Hessian is a dynamically-typed, binary serialization and web services protocol
 * designed for object-oriented transmission.
 *
 * @author hsien
 */
@LoadLevel(name = SerializerType.HESSIAN)
public class HessianSerializer implements Serializer {
    @Override
    public <T extends Serializable> byte[] serialize(T obj) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(outputStream);
            hessianOutput.writeObject(obj);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException(SerializerType.HESSIAN, "Failed to serialize", e);
        }
    }

    @Override
    public <T extends Serializable> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(inputStream);
            Object o = hessianInput.readObject();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException(SerializerType.HESSIAN, "Failed to deserialize", e);
        }
    }
}
