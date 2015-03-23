package ibessonov.ss;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.util.stream.IntStream.range;
import java.util.stream.Stream;

/**
 *
 * @author ibessonov
 */
final class Util {

    public static List<int[]> generateSat(Field field) {
        // raw +1-in-k-SAT
        return Stream.concat(
                Stream.of(field.rows()).flatMap(row
                        -> range(0, 9).filter(row::hasNo).mapToObj(x
                                -> Stream.of(row.cells())
                                .filter(Field.Cell::empty)
                                .filter(cell -> cell.block().hasNo(x))
                                .filter(cell -> cell.column().hasNo(x))
                                .mapToInt(cell -> cell.index(x))
                        )
                ),
                Stream.concat(
                        Stream.of(field.columns()).flatMap(column
                                -> range(0, 9).filter(column::hasNo).mapToObj(x
                                        -> Stream.of(column.cells())
                                        .filter(Field.Cell::empty)
                                        .filter(cell -> cell.block().hasNo(x))
                                        .filter(cell -> cell.row().hasNo(x))
                                        .mapToInt(cell -> cell.index(x))
                                )
                        ),
                        Stream.concat(
                                Stream.of(field.blocks()).flatMap(block
                                        -> range(0, 9).filter(block::hasNo).mapToObj(x
                                                -> Stream.of(block.cells())
                                                .filter(Field.Cell::empty)
                                                .filter(cell -> cell.row().hasNo(x))
                                                .filter(cell -> cell.column().hasNo(x))
                                                .mapToInt(cell -> cell.index(x))
                                        )
                                ),
                                Stream.of(field.rows()).flatMap(row
                                        -> (Stream<IntStream>) Stream.of(row.cells())
                                        .filter(Field.Cell::empty).map(cell
                                                -> range(0, 9)
                                                .filter(row::hasNo)
                                                .filter(cell.column()::hasNo)
                                                .filter(cell.block()::hasNo)
                                                .map(cell::index)
                                        )
                                )
                        )
                )
        ).map(IntStream::toArray).filter(a -> a.length > 0).collect(Collectors.toList());
    }

    public static List<int[]> generateExcludeSat(Field field) {
        return Stream.concat(
                Stream.of(field.rows()).flatMap(row
                        -> range(0, 9).mapToObj(x
                                -> Stream.of(row.cells())
                                .mapToInt(cell -> cell.index(x))
                        )
                ),
                Stream.concat(
                        Stream.of(field.columns()).flatMap(column
                                -> range(0, 9).mapToObj(x
                                        -> Stream.of(column.cells())
                                        .mapToInt(cell -> cell.index(x))
                                )
                        ),
                        Stream.concat(
                                Stream.of(field.blocks()).flatMap(block
                                        -> range(0, 9).mapToObj(x
                                                -> Stream.of(block.cells())
                                                .mapToInt(cell -> cell.index(x))
                                        )
                                ),
                                Stream.of(field.rows()).flatMap(row
                                        -> (Stream<IntStream>) Stream.of(row.cells()).map(cell
                                                -> range(0, 9).map(cell::index)
                                        )
                                )
                        )
                )
        ).map(IntStream::toArray).flatMap(array
                -> range(0, 9).mapToObj(i
                        -> range(0, i).mapToObj(j
                                -> new int[]{array[i], array[j]}
                        )
                )
        ).flatMap(Function.identity()).map(pair
                -> present(field, pair[0]) && present(field, pair[1])
                        ? new int[]{}
                        : !present(field, pair[0]) && !present(field, pair[1])
                                ? pair
                                : present(field, pair[0])
                                        ? new int[]{pair[1]}
                                        : new int[]{pair[0]}
        ).filter(a -> a.length > 0).collect(Collectors.toList());
    }

    public static int i(int index) {
        return index / 9 / 9;
    }

    public static int j(int index) {
        return index / 9 % 9;
    }

    public static int x(int index) {
        return index % 9;
    }

    public static int index(int i, int j, int x) {
        return (i * 9 + j) * 9 + x;
    }

    private static boolean present(Field field, int index) {
        return field.cell(i(index), j(index)).value() == x(index) + 1;
    }
}
