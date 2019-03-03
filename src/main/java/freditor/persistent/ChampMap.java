package freditor.persistent;

import java.util.Arrays;

import static java.lang.Integer.bitCount;

public class ChampMap<K, V> {
    private final Object[] array;
    private final int usedKeyValues;
    private final int usedSubMaps;

    private ChampMap(Object[] array, int usedKeyValues, int usedSubMaps) {
        this.array = array;
        this.usedKeyValues = usedKeyValues;
        this.usedSubMaps = usedSubMaps;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ChampMap<K, V> empty() {
        return (ChampMap<K, V>) EMPTY;
    }

    private static final ChampMap<?, ?> EMPTY = new ChampMap(new Object[0], 0, 0);

    public static <K, V> ChampMap<K, V> of(K key, V value) {
        return ChampMap.<K, V>empty().put(key, value);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).put(k8, v8);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).put(k8, v8).put(k9, v9);
    }

    public static <K, V> ChampMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return ChampMap.<K, V>empty().put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5).put(k6, v6).put(k7, v7).put(k8, v8).put(k9, v9).put(k10, v10);
    }

    private int keyIndex(int bitmask) {
        return bitCount(usedKeyValues & (bitmask - 1)) * 2;
    }

    private int subMapIndex(int bitmask) {
        return bitCount(usedKeyValues) * 2 + bitCount(usedSubMaps & (bitmask - 1));
    }

    @SuppressWarnings("unchecked")
    public K getKey(K key) {
        return (K) get(key, key.hashCode(), 0, 0);
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        return (V) get(key, key.hashCode(), 0, 1);
    }

    @SuppressWarnings("unchecked")
    private Object get(K key, int hash, int shift, int keyValueSelector) {
        if (shift >= 32) {
            // hash exhausted, fall back to linear search
            final int n = array.length;
            for (int i = 0; i < n; i += 2) {
                if (array[i].equals(key)) {
                    return array[i + keyValueSelector];
                }
            }
            return null;
        }

        int index = (hash >>> shift) & 31;
        int bitmask = 1 << index;

        if ((usedSubMaps & bitmask) != 0) {
            ChampMap<K, V> subMap = (ChampMap<K, V>) array[subMapIndex(bitmask)];
            return subMap.get(key, hash, shift + 5, keyValueSelector);
        }

        if ((usedKeyValues & bitmask) != 0) {
            int keyIndex = keyIndex(bitmask);
            if (array[keyIndex].equals(key)) {
                return array[keyIndex + keyValueSelector];
            }
        }

        return null;
    }

    public ChampMap<K, V> put(K key, V value) {
        return put(key, value, key.hashCode(), 0);
    }

    @SuppressWarnings("unchecked")
    private ChampMap<K, V> put(K key, V value, int hash, int shift) {
        if (shift >= 32) {
            // hash exhausted, fall back to linear search
            final int n = array.length;
            for (int i = 0; i < n; i += 2) {
                if (array[i].equals(key)) {
                    // replace existing value
                    Object[] a = array.clone();
                    a[i + 1] = value;
                    return new ChampMap<>(a, -1, -1);
                }
            }
            // append new value
            Object[] a = Arrays.copyOf(array, n + 2);
            a[n] = key;
            a[n + 1] = value;
            return new ChampMap<>(a, -1, -1);
        }

        int index = (hash >>> shift) & 31;
        int bitmask = 1 << index;
        int keyIndex = keyIndex(bitmask);
        int subMapIndex = subMapIndex(bitmask);

        if ((usedSubMaps & bitmask) != 0) {
            Object[] a = array.clone();
            ChampMap<K, V> subMap = (ChampMap<K, V>) a[subMapIndex];
            a[subMapIndex] = subMap.put(key, value, hash, shift + 5);
            return new ChampMap<>(a, usedKeyValues, usedSubMaps);
        }

        if ((usedKeyValues & bitmask) == 0) {
            Object[] a = new Object[array.length + 2];
            System.arraycopy(array, 0, a, 0, keyIndex);
            System.arraycopy(array, keyIndex, a, keyIndex + 2, array.length - keyIndex);
            a[keyIndex] = key;
            a[keyIndex + 1] = value;
            return new ChampMap<>(a, usedKeyValues | bitmask, usedSubMaps);
        }

        if (array[keyIndex].equals(key)) {
            Object[] a = array.clone();
            a[keyIndex + 1] = value;
            return new ChampMap<>(a, usedKeyValues, usedSubMaps);
        }

        Object[] a = new Object[array.length - 2 + 1];
        System.arraycopy(array, 0, a, 0, keyIndex);
        System.arraycopy(array, keyIndex + 2, a, keyIndex, subMapIndex - (keyIndex + 2));
        System.arraycopy(array, subMapIndex, a, subMapIndex - 1, array.length - subMapIndex);

        Object oldKey = array[keyIndex];
        Object oldValue = array[keyIndex + 1];
        a[subMapIndex - 2] = empty()
                .put(oldKey, oldValue, oldKey.hashCode(), shift + 5)
                .put(key, value, hash, shift + 5);

        return new ChampMap<>(a, usedKeyValues ^ bitmask, usedSubMaps | bitmask);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChampMap && equals(this, (ChampMap<?, ?>) obj);
    }

    private static boolean equals(ChampMap<?, ?> a, ChampMap<?, ?> b) {
        return a.usedKeyValues == b.usedKeyValues
                && a.usedSubMaps == b.usedSubMaps
                && (a.usedKeyValues != a.usedSubMaps
                ? Arrays.equals(a.array, b.array)
                : equalEntries(a.array, b.array));
    }

    private static boolean equalEntries(Object[] map1, Object[] map2) {
        final int n = map1.length;
        if (n != map2.length) {
            return false;
        }
        outer:
        for (int i = 0; i < n; i += 2) {
            Object key = map1[i];
            for (int k = 0; k < n; k += 2) {
                if (key.equals(map2[k])) {
                    if (map1[i + 1].equals(map2[k + 1])) {
                        // found entry
                        continue outer;
                    } else {
                        // found key mapped to different value
                        return false;
                    }
                }
            }
            // did not find key
            return false;
        }
        // found all entries
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Object o : array) {
            hash += o.hashCode();
        }
        return hash;
    }
}
