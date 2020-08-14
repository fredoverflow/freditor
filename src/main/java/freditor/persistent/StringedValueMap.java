package freditor.persistent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.lang.Integer.bitCount;

/**
 * A memory-optimized version of ChampMap where keys are not stored,
 * but computed by calling toString on the corresponding value.
 * StringedValueMaps CANNOT BE NESTED due to instanceof checks!
 */
public class StringedValueMap<V> implements Iterable<V> {
    private final Object[] array;
    private final int used;

    private StringedValueMap(Object[] array, int used) {
        this.array = array;
        this.used = used;
    }

    @SuppressWarnings("unchecked")
    public static <V> StringedValueMap<V> empty() {
        return (StringedValueMap<V>) EMPTY;
    }

    private static final StringedValueMap<?> EMPTY = new StringedValueMap<>(new Object[0], 0);

    public static <V> StringedValueMap<V> of(V value) {
        return StringedValueMap.<V>empty().put(value);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2) {
        return StringedValueMap.<V>empty().put(v1).put(v2);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5, V v6) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5).put(v6);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5, V v6, V v7) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5).put(v6).put(v7);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5, V v6, V v7, V v8) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5).put(v6).put(v7).put(v8);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5, V v6, V v7, V v8, V v9) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5).put(v6).put(v7).put(v8).put(v9);
    }

    public static <V> StringedValueMap<V> of(V v1, V v2, V v3, V v4, V v5, V v6, V v7, V v8, V v9, V v10) {
        return StringedValueMap.<V>empty().put(v1).put(v2).put(v3).put(v4).put(v5).put(v6).put(v7).put(v8).put(v9).put(v10);
    }

    @SafeVarargs
    public static <V> StringedValueMap<V> of(V... values) {
        StringedValueMap<V> temp = empty();
        for (V value : values) {
            temp = temp.put(value);
        }
        return temp;
    }

    public boolean contains(Object key) {
        return get(key) != null;
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public V get(Object key) {
        return get(key.toString());
    }

    public V get(String key) {
        return get(key, key.hashCode(), 0);
    }

    @SuppressWarnings("unchecked")
    private V get(String key, int hash, int shift) {
        if (shift >= 32) {
            // hash exhausted, fall back to binary search
            int index = binarySearch(key);
            return index >= 0 ? (V) array[index] : null;
        }

        int bitmask = bitmask(hash, shift);

        if ((used & bitmask) != 0) {
            int index = index(bitmask);
            Object value = array[index];
            if (value instanceof StringedValueMap) {
                return ((StringedValueMap<V>) value).get(key, hash, shift + 5);
            }
            if (value.toString().equals(key)) {
                return (V) value;
            }
        }

        return null;
    }

    private int binarySearch(String key) {
        int left = 0; // inclusive
        int right = array.length; // exclusive
        while (left < right) {
            int middle = (left + right) >>> 1;
            int comparison = array[middle].toString().compareTo(key);
            if (comparison < 0) {
                left = middle + 1; // inclusive
            } else if (comparison > 0) {
                right = middle; // exclusive
            } else {
                return middle;
            }
        }
        return ~left;
    }

    private int bitmask(int hash, int shift) {
        int bitPosition = (hash >>> shift) & 31;
        return 1 << bitPosition;
    }

    private int index(int bitmask) {
        return bitCount(used & (bitmask - 1));
    }

    public StringedValueMap<V> put(V value) {
        String key = value.toString();
        return put(key, value, key.hashCode(), 0);
    }

    @SuppressWarnings("unchecked")
    private StringedValueMap<V> put(String key, V value, int hash, int shift) {
        if (shift >= 32) {
            // hash exhausted, fall back to binary search
            Object[] a;
            int index = binarySearch(key);
            if (index >= 0) {
                // replace existing value
                a = array.clone();
                a[index] = value;
            } else {
                // insert new value
                index = ~index;
                a = new Object[array.length + 1];
                System.arraycopy(array, 0, a, 0, index);
                a[index] = value;
                System.arraycopy(array, index, a, index + 1, array.length - index);
            }
            return new StringedValueMap<>(a, 0);
        }

        int bitmask = bitmask(hash, shift);
        int index = index(bitmask);

        if ((used & bitmask) == 0) {
            // insert new value
            Object[] a = new Object[array.length + 1];
            System.arraycopy(array, 0, a, 0, index);
            a[index] = value;
            System.arraycopy(array, index, a, index + 1, array.length - index);
            return new StringedValueMap<>(a, used | bitmask);
        }

        Object[] a = array.clone();
        Object oldValue = a[index];

        if (oldValue instanceof StringedValueMap) {
            // put into submap
            a[index] = ((StringedValueMap<V>) oldValue).put(key, value, hash, shift + 5);
        } else if (oldValue.toString().equals(key)) {
            // replace existing value
            a[index] = value;
        } else {
            // create new submap
            String oldKey = oldValue.toString();
            a[index] = empty()
                    .put(oldKey, oldValue, oldKey.hashCode(), shift + 5)
                    .put(key, value, hash, shift + 5);
        }

        return new StringedValueMap<>(a, used);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StringedValueMap && equals(this, (StringedValueMap<?>) obj);
    }

    private static boolean equals(StringedValueMap<?> a, StringedValueMap<?> b) {
        return a.used == b.used && Arrays.equals(a.array, b.array);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Object value : array) {
            hash += value.hashCode();
        }
        return hash;
    }

    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super V> consumer) {
        for (Object object : array) {
            if (object instanceof StringedValueMap) {
                ((StringedValueMap<V>) object).forEach(consumer);
            } else {
                consumer.accept((V) object);
            }
        }
    }

    @Override
    public Iterator<V> iterator() {
        return array.length == 0 ? Collections.emptyIterator() : new StringedValueMapIterator<>(this);
    }

    private static class StringedValueMapIterator<V> implements Iterator<V> {
        private Object[][] stack;
        private int top;

        private long index;
        private int shift;

        @SuppressWarnings("unchecked")
        public StringedValueMapIterator(StringedValueMap<V> map) {
            stack = new Object[8][];
            stack[0] = map.array;
            Object obj = map.array[0];
            // descend
            while (obj instanceof StringedValueMap) {
                map = (StringedValueMap<V>) obj;
                stack[++top] = map.array;
                obj = map.array[0];
            }
            shift = top * 5;
        }

        @Override
        public boolean hasNext() {
            return top >= 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V next() {
            int i = (int) (index >>> shift);
            Object result = stack[top][i];
            // ascend
            while (++i == stack[top].length) {
                if (--top < 0) return (V) result;

                index &= (1L << shift) - 1;
                shift -= 5;
                i = (int) (index >>> shift);
            }
            index += 1L << shift;
            // descend
            while (stack[top][i] instanceof StringedValueMap) {
                StringedValueMap<V> map = (StringedValueMap<V>) stack[top][i];
                stack[++top] = map.array;
                shift += 5;
                i = 0;
            }
            return (V) result;
        }
    }
}
