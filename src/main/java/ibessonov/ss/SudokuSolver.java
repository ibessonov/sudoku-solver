/*
 * Copyright (c) 2018 Ivan Bessonov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ibessonov.ss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ibessonov.ss.Util.*;
import static java.util.stream.Collectors.toSet;

/**
 * @author ibessonov
 */
public final class SudokuSolver {

    private final Field field;
    private double precision = 1e-3;
    private boolean loggingEnabled = false;

    private List<int[]> positives;
    private List<int[]> negatives;

    public static SudokuSolverBuilder of(String sudoku) {
        return new SudokuSolverBuilder(sudoku);
    }

    SudokuSolver(String sudoku) {
        field = new Field(sudoku);
    }

    void init() {
        positives = generatePositiveSat(field);
        negatives = generateNegativeSat(field);
    }

    public void setPrecision(double precision) {
        if (precision <= 0 || precision > 1) {
            throw new IllegalArgumentException("precision");
        }
        this.precision = precision;
    }

    public void enableLogging(boolean enable) {
        this.loggingEnabled = enable;
    }

    public Field solve() {
        int[][] result = new int[9][9];

        // initial values
        for (Field.Row row : field.rows()) {
            for (Field.Cell cell : row.cells()) {
                result[cell.i][cell.j] = cell.value();
            }
        }

        // values determined while system solving
        for (int index : solve0(positives, negatives)) {
            result[i(index)][j(index)] = x(index) + 1;
        }

        return new Field(result);
    }

    private int[] solve0(List<int[]> positives, List<int[]> negatives) {

        // +1-in-k-SAT into k-SAT
        Set<IntArray> set = positives.stream().map(IntArray::new).collect(toSet());

        // indexing
        final Map<Integer, Integer> mapping = new HashMap<>();
        set.forEach(array
                -> IntStream.of(array.values())
                .filter(index -> !mapping.containsKey(index)) // rely on execution order
                .forEach(index -> mapping.put(index, mapping.size()))
        );

        int N = mapping.size();
        int[] reverseMapping = new int[N];
        mapping.entrySet().forEach(entry
                -> reverseMapping[entry.getValue()] = entry.getKey()
        );

        Set<IntArray> excluded = negatives.stream()
                .filter(a -> IntStream.of(a).allMatch(mapping::containsKey))
                .map(IntArray::new).collect(toSet());

        // preparing for calculation
        Function<Boolean, Function<IntArray, PositiveLiteral[]>> valueMapper = present -> array
                -> IntStream.of(array.values()).mapToObj(x
                        -> new PositiveLiteral(mapping.get(x), present)
                ).toArray(PositiveLiteral[]::new);
        PositiveLiteral[][] disjunctions = Stream.concat(
                set.stream().map(valueMapper.apply(true)),
                excluded.stream().map(valueMapper.apply(false))
        ).toArray(PositiveLiteral[][]::new);

        int[] result = new CashKarpSolver(N, disjunctions, precision, loggingEnabled).solve();
        return IntStream.of(result).map(i -> reverseMapping[i]).toArray();
    }
}
