package github.hsien.rpc.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;


/**
 * Collection utils
 *
 * @author hsien
 */
public abstract class CollectionUtils {
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>((int) (expectedSize / DEFAULT_LOAD_FACTOR), DEFAULT_LOAD_FACTOR);
    }

    public static <T> T firstElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).first();
        }

        Iterator<T> it = set.iterator();
        T first = null;
        if (it.hasNext()) {
            first = it.next();
        }
        return first;
    }

    public static <T> T firstElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public static <T> T lastElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).last();
        }
        Iterator<T> it = set.iterator();
        T last = null;
        while (it.hasNext()) {
            last = it.next();
        }
        return last;
    }

    public static <T> T lastElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static <T> T firstElementInSafe(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        while (list.size() > 0) {
            try {
                return list.get(0);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return null;
    }

    public static <T> T lastElementInSafe(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        int size;
        while ((size = list.size()) > 0) {
            try {
                return list.get(size - 1);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return null;
    }
}
