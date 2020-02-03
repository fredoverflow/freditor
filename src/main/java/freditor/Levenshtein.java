package freditor;

public class Levenshtein {
    public static int distance(String a, String b) {
        return a.length() < b.length() ? distance_(b, a) : distance_(a, b);
    }

    private static int distance_(String big, String small) {
        int begin = 0;
        int m = big.length();
        int n = small.length();

        // skip common prefix
        while (begin < n && big.charAt(begin) == small.charAt(begin)) {
            ++begin;
        }

        // skip common suffix
        while (begin < n && big.charAt(m - 1) == small.charAt(n - 1)) {
            --m;
            --n;
        }

        return distance_(begin, big, m - begin, small, n - begin);
    }

    private static int distance_(int begin, String big, int m, String small, int n) {
        int[] previous = new int[n + 1];
        int[] current = new int[n + 1];

        for (int i = 0; i <= n; ++i) {
            previous[i] = i;
        }

        for (int i = 0; i < m; ++i) {
            current[0] = i + 1;

            for (int j = 0; j < n; ++j) {
                int delete = previous[j + 1] + 1;
                int insert = current[j] + 1;
                int change = previous[j] + (big.charAt(begin + i) != small.charAt(begin + j) ? 1 : 0);

                current[j + 1] = Math.min(Math.min(delete, insert), change);
            }

            int[] temp = previous;
            previous = current;
            current = temp;
        }

        return previous[n];
    }
}
