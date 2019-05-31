package freditor.persistent;

import java.util.Arrays;

import static java.lang.Integer.bitCount;

/**
 * HamtSets CANNOT BE NESTED due to instanceof checks!
 */
public class HamtSet<K extends Comparable<K>> {
    private final Object[] array;
    private final int used;
    private final int hashCode;

    private HamtSet(Object[] array, int used, int hashCode) {
        this.array = array;
        this.used = used;
        this.hashCode = hashCode;
    }

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>> HamtSet<K> empty() {
        return (HamtSet<K>) EMPTY;
    }

    private static final HamtSet<?> EMPTY = new HamtSet(new Object[0], 0, 0);

    public static <K extends Comparable<K>> HamtSet<K> of(K value) {
        return HamtSet.<K>empty().put(value);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2) {
        return HamtSet.<K>empty().put(k1).put(k2);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5, K k6) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5).put(k6);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5, K k6, K k7) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5).put(k6).put(k7);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5, K k6, K k7, K k8) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5).put(k6).put(k7).put(k8);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5, K k6, K k7, K k8, K k9) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5).put(k6).put(k7).put(k8).put(k9);
    }

    public static <K extends Comparable<K>> HamtSet<K> of(K k1, K k2, K k3, K k4, K k5, K k6, K k7, K k8, K k9, K k10) {
        return HamtSet.<K>empty().put(k1).put(k2).put(k3).put(k4).put(k5).put(k6).put(k7).put(k8).put(k9).put(k10);
    }

    @SafeVarargs
    public static <K extends Comparable<K>> HamtSet<K> of(K... values) {
        HamtSet<K> temp = empty();
        for (K value : values) {
            temp = temp.put(value);
        }
        return temp;
    }

    public K get(K key) {
        return get(key, key.hashCode(), 0);
    }

    @SuppressWarnings("unchecked")
    private K get(K key, int hash, int shift) {
        if (shift >= 32) {
            // hash exhausted, fall back to binary search
            int index = binarySearch(key);
            return index >= 0 ? (K) array[index] : null;
        }

        int bitmask = bitmask(hash, shift);

        if ((used & bitmask) != 0) {
            int index = index(bitmask);
            Object value = array[index];
            if (value instanceof HamtSet) {
                return ((HamtSet<K>) value).get(key, hash, shift + 5);
            }
            if (value.equals(key)) {
                return (K) value;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private int binarySearch(K key) {
        int left = 0; // inclusive
        int right = array.length; // exclusive
        while (left < right) {
            int middle = (left + right) >>> 1;
            int comparison = ((K) array[middle]).compareTo(key);
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

    public HamtSet<K> put(K key) {
        return put(key, key.hashCode(), 0);
    }

    @SuppressWarnings("unchecked")
    private HamtSet<K> put(K key, int hash, int shift) {
        if (shift >= 32) {
            // hash exhausted, fall back to binary search
            Object[] a;
            int index = binarySearch(key);
            if (index >= 0) {
                // replace existing value
                a = array.clone();
                a[index] = key;
                hash -= array[index].hashCode();
            } else {
                // insert new value
                index = ~index;
                a = new Object[array.length + 1];
                System.arraycopy(array, 0, a, 0, index);
                System.arraycopy(array, index, a, index + 1, array.length - index);
            }
            return new HamtSet<>(a, 0, hashCode + hash);
        }

        int bitmask = bitmask(hash, shift);
        int index = index(bitmask);

        if ((used & bitmask) == 0) {
            // insert new value
            Object[] a = new Object[array.length + 1];
            System.arraycopy(array, 0, a, 0, index);
            System.arraycopy(array, index, a, index + 1, array.length - index);
            a[index] = key;
            return new HamtSet<>(a, used | bitmask, hashCode + hash);
        }

        Object[] a = array.clone();
        Object oldValue = a[index];

        if (oldValue instanceof HamtSet) {
            // put into submap
            a[index] = ((HamtSet<K>) oldValue).put(key, hash, shift + 5);
        } else if (oldValue.equals(key)) {
            // replace existing value
            a[index] = key;
            hash -= array[index].hashCode();
        } else {
            // create new submap
            a[index] = HamtSet.<K>empty()
                    .put((K) oldValue, oldValue.hashCode(), shift + 5)
                    .put(key, hash, shift + 5);
        }

        return new HamtSet<>(a, used, hashCode + hash);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HamtSet && equals(this, (HamtSet<?>) obj);
    }

    private static boolean equals(HamtSet<?> a, HamtSet<?> b) {
        return a.used == b.used && Arrays.equals(a.array, b.array);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean isEmpty() {
        return used == 0;
    }

    @SuppressWarnings("unchecked")
    public K first() {
        Object value = array[0];
        if (value instanceof HamtSet) {
            return ((HamtSet<K>) value).first();
        } else {
            return (K) value;
        }
    }
}
