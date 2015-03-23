package ibessonov.ss;

import java.util.Arrays;

/**
 *
 * @author ibessonov
 */
final class IntArray {

    private final int[] values;

    public IntArray(int... values) {
        this.values = values.clone();
        Arrays.sort(this.values);
    }

    public int at(int i) {
        return values[i];
    }

    public int size() {
        return values.length;
    }

    public int[] values() {
        return values.clone();
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof IntArray) && Arrays.equals(values, ((IntArray) that).values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
