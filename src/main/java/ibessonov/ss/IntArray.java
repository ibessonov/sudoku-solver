package ibessonov.ss;

import java.util.Arrays;

/**
 * @author ibessonov
 */
final class IntArray {

    private final int[] values;
    private final int hash;

    IntArray(int... values) {
        this.values = values;
        Arrays.sort(values);
        this.hash = Arrays.hashCode(values);
    }

    int[] values() {
        return values;
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof IntArray) && Arrays.equals(values, ((IntArray) that).values);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
