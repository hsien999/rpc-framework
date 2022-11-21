package github.hsien.rpc.common.singleton;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton factory
 * <p>
 * A factory that holds a map associated class name with singleton instance
 *
 * @author hsien
 */
public class SingletonFactory {
    private static final ConcurrentHashMap<String, SingletonHolder<?>> FACTORY = new ConcurrentHashMap<>();

    /**
     * Get the singleton instance
     *
     * @param clazz class type
     * @param <T>   type parameter of instance to be got
     * @return singleton instance
     * @see #getInstance
     */
    public static <T> T getInstance(@NonNull Class<T> clazz) {
        return getInstance(clazz, null, null);
    }

    /**
     * Get the singleton instance
     *
     * @param clazz    class type
     * @param argTypes argument types
     * @param args     argument values
     * @param <T>      type parameter of instance to be got
     * @return singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(@NonNull Class<T> clazz, Class<?>[] argTypes, Object[] args) {
        SingletonHolder<T> holder = (SingletonHolder<T>) FACTORY.computeIfAbsent(clazz.toString(),
            k -> new SingletonHolder<>());
        return holder.setIfAbsent(() -> createNewInstance(clazz, argTypes, args));
    }

    private static <T> T createNewInstance(Class<T> clazz, Class<?>[] argTypes, Object[] args) {
        T instance;
        try {
            if (argTypes != null && args != null) {
                Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
                instance = constructor.newInstance(args);
            } else {
                instance = clazz.newInstance();
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }
}
