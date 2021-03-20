package freditor;

import java.util.function.Supplier;

public class Benchmark {
    public static void measure(Runnable runnable) {
        long before = System.nanoTime();
        runnable.run();
        long after = System.nanoTime();
        System.out.println(measurement(before, after));
    }

    public static <T> T measure(Supplier<T> supplier) {
        long before = System.nanoTime();
        T result = supplier.get();
        long after = System.nanoTime();
        System.out.println(measurement(before, after));
        return result;
    }

    private static String measurement(long before, long after) {
        long duration = after - before;
        String unit = "ns";
        if (duration >= 100_000) {
            duration /= 1_000;
            unit = "µs";
            if (duration >= 100_000) {
                duration /= 1_000;
                unit = "ms";
                if (duration >= 100_000) {
                    duration /= 1_000;
                    unit = "s";
                }
            }
        }
        return duration + unit;
    }
}
