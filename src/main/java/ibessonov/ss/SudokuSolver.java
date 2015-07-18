package ibessonov.ss;

import static ibessonov.ss.Util.generateExcludeSat;
import static ibessonov.ss.Util.generateSat;
import static ibessonov.ss.Util.i;
import static ibessonov.ss.Util.j;
import static ibessonov.ss.Util.log;
import static ibessonov.ss.Util.x;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author ibessonov
 */
public final class SudokuSolver {

    private final Field field;
    private double precision = 1e-3;
    private boolean loggingEnabled = false;

    public static SudokuSolverBuilder of(String sudoku) {
        return new SudokuSolverBuilder(sudoku);
    }

    SudokuSolver(String sudoku) {
        field = new Field(sudoku);
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
        final List<int[]> disjunctions = generateSat(field);
        List<int[]> excludes = generateExcludeSat(field);
        final int[][] result = new int[9][9];

        // initial values
        for (Field.Row row : field.rows()) {
            for (Field.Cell cell : row.cells()) {
                result[cell.i][cell.j] = cell.value();
            }
        }

        // values determined while system solving
        int[] indexes = solve0(disjunctions, excludes);

        for (int index : indexes) {
            result[i(index)][j(index)] = x(index) + 1;
        }

        return new Field(result);
    }

    private int[] solve0(List<int[]> positives, List<int[]> negatives) {

        // +1-in-k-SAT into k-SAT
        Set<IntArray> set = positives.stream().map(IntArray::new).collect(Collectors.toSet());

        // indexing
        final Map<Integer, Integer> mapping = new HashMap<>();
        set.stream().forEach(array
                -> IntStream.of(array.values())
                .filter(index -> !mapping.containsKey(index)) // rely on execution order
                .forEach(index -> mapping.put(index, mapping.size()))
        );

        int N = mapping.size();
        int[] reverseMapping = new int[N];
        mapping.entrySet().stream().forEach(entry
                -> reverseMapping[entry.getValue()] = entry.getKey()
        );

        Set<IntArray> excluded = negatives.stream()
                .filter(a -> IntStream.of(a).allMatch(mapping::containsKey))
                .map(IntArray::new).collect(Collectors.toSet());

        // preparing for calculation
        Function<Boolean, Function<IntArray, PositiveLiteral[]>> valueMapper = present -> array
                -> IntStream.of(array.values()).mapToObj(x
                        -> new PositiveLiteral(mapping.get(x), present)
                ).toArray(PositiveLiteral[]::new);
        PositiveLiteral[][] disjunctions = Stream.concat(
                set.stream().map(valueMapper.apply(true)),
                excluded.stream().map(valueMapper.apply(false))
        ).toArray(PositiveLiteral[][]::new);
        if (loggingEnabled) {
            log("Complexity approximation: %.3f\n", 1d * disjunctions.length / N);
        }
        int[] result = new CashKarpSolver(N, disjunctions, precision, loggingEnabled).solve();
        return IntStream.of(result).map(i -> reverseMapping[i]).toArray();
    }
}
