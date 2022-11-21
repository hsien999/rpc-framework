package github.hsien.rpc.common.singleton;

import java.util.function.Supplier;

/**
 * Holder that holds a single instance in thread-safe
 *
 * @author hsien
 */
public final class SingletonHolder<T> {
    private volatile T item;

    /**
     * Returns the holden value
     *
     * @return {@link #item}
     */
    public T get() {
        return item;
    }

    /**
     * The {@link #item} is set to the result returned by the supplier if item is null, which is thread-safe
     * by double-checking the lock
     * <p> Note: this method does not verify whether the specified supplier returns null or throws exception
     *
     * @param supplier a function that return an item
     * @return current item after setting
     */
    public T setIfAbsent(Supplier<? extends T> supplier) {
        if (item == null) {
            synchronized (this) {
                if (item == null) {
                    item = supplier.get();
                }
            }
        }
        return item;
    }
}