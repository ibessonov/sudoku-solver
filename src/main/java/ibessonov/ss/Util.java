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

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;

/**
 * @author ibessonov
 * Don't even try to rewrite this beast with loops...
 */
final class Util {

    public static void log(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static List<int[]> generatePositiveSat(Field field) {
        // raw +1-in-k-SAT
        return concat(
                Stream.of(field.rows()).flatMap(row
                        -> range(0, 9).filter(row::hasNo).mapToObj(x
                                -> Stream.of(row.cells())
                                .filter(Field.Cell::empty)
                                .filter(cell -> cell.block().hasNo(x))
                                .filter(cell -> cell.column().hasNo(x))
                                .mapToInt(cell -> cell.index(x))
                        )
                ),
                concat(
                        Stream.of(field.columns()).flatMap(column
                                -> range(0, 9).filter(column::hasNo).mapToObj(x
                                        -> Stream.of(column.cells())
                                        .filter(Field.Cell::empty)
                                        .filter(cell -> cell.block().hasNo(x))
                                        .filter(cell -> cell.row().hasNo(x))
                                        .mapToInt(cell -> cell.index(x))
                                )
                        ),
                        concat(
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
        ).parallel().map(IntStream::toArray).filter(a -> a.length > 0).collect(toList());
    }

    public static List<int[]> generateNegativeSat(Field field) {
        return concat(
                Stream.of(field.rows()).flatMap(row
                        -> range(0, 9).mapToObj(x
                                -> Stream.of(row.cells())
                                .mapToInt(cell -> cell.index(x))
                        )
                ),
                concat(
                        Stream.of(field.columns()).flatMap(column
                                -> range(0, 9).mapToObj(x
                                        -> Stream.of(column.cells())
                                        .mapToInt(cell -> cell.index(x))
                                )
                        ),
                        concat(
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
        ).parallel().map(IntStream::toArray).flatMap(array
                -> range(0, 9).mapToObj(i
                        -> range(0, i).mapToObj(j
                                -> new int[]{array[i], array[j]}
                        )
                )
        ).flatMap(s -> s).map(pair
                -> present(field, pair[0])
                        ? (present(field, pair[1])
                            ? new int[]{}
                            : new int[]{pair[1]}
                        ) : (present(field, pair[1])
                            ? new int[]{pair[0]}
                            : pair
                        )
        ).filter(a -> a.length > 0).collect(toList());
    }

    static int i(int index) {
        return index / 9 / 9;
    }

    static int j(int index) {
        return index / 9 % 9;
    }

    static int x(int index) {
        return index % 9;
    }

    static int index(int i, int j, int x) {
        return (i * 9 + j) * 9 + x;
    }

    private static boolean present(Field field, int index) {
        return field.cell(i(index), j(index)).value() == x(index) + 1;
    }
}
