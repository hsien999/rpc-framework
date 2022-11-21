package github.hsien.rpc.core.serialize;

import github.hsien.rpc.common.loader.annotion.SPI;

import java.io.Serializable;


/**
 * Serializer interface
 *
 * @author hsien
 */
@SPI
public interface Serializer {
    /**
     * Serialize object to bytes array
     *
     * @param obj object to be serialized
     * @return bytes array
     * @throws SerializeException serialize exception
     */
    <T extends Serializable> byte[] serialize(T obj);

    /**
     * Deserialize object from given bytes array and class type
     *
     * @param bytes bytes array
     * @param clazz class type
     * @param <T>   type parameter indicates the type of object to be deserialized
     * @return object
     * @throws SerializeException serialize exception
     */
    <T extends Serializable> T deserialize(byte[] bytes, Class<T> clazz);
}
