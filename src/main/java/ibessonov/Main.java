package ibessonov;

import ibessonov.ss.Field;
import ibessonov.ss.Field.Cell;
import ibessonov.ss.Field.Row;
import ibessonov.ss.SudokuSolver;

import java.util.stream.IntStream;

import static java.lang.Runtime.getRuntime;

/**
 * @author ibessonov
 */
public class Main {

    public static void main(String[] args) {

        /*
         * "Very Hard Sudoku" - Google search result #1
         *     Solved
         * 7 6 3 4 2 1 5 9 8
         * 8 1 2 9 7 5 4 6 3
         * 9 5 4 3 8 6 7 2 1
         * 4 9 5 2 6 3 1 8 7
         * 6 8 1 5 4 7 9 3 2
         * 2 3 7 8 1 9 6 5 4
         * 1 2 6 7 5 8 3 4 9
         * 5 4 9 1 3 2 8 7 6
         * 3 7 8 6 9 4 2 1 5
         */
        final String field = "003020090002005000904080001400060080000507000030010004100050309000100800070090200";

        /*
         * "Very Hard Sudoku" - Google search result #2
         *     Solved
         * 9 8 7 6 5 4 3 2 1
         * 2 4 6 1 7 3 9 8 5
         * 3 5 1 9 2 8 7 4 6
         * 1 2 8 5 3 7 6 9 4
         * 6 3 4 8 9 2 1 5 7
         * 7 9 5 4 6 1 8 3 2
         * 5 1 9 2 8 6 4 7 3
         * 4 7 2 3 1 9 5 6 8
         * 8 6 3 7 4 5 2 1 9
         */
//        final String field = "000000000000003085001020000000507000004000100090000000500000073002010000000040009";

        /*
         * Hardest sudoku from here: https://github.com/hpenedones/sudoku
         *      Solved
         * 2 9 7 5 4 3 1 8 6
         * 4 8 1 7 2 6 3 9 5
         * 3 5 6 1 8 9 2 7 4
         * 5 7 2 6 3 8 4 1 9
         * 9 6 3 4 5 1 8 2 7
         * 8 1 4 9 7 2 6 5 3
         * 6 3 8 2 9 7 5 4 1
         * 1 4 9 8 6 5 7 3 2
         * 7 2 5 3 1 4 9 6 8
         */
//        final String field = "200500080001020000000000000070008000003000020000070600600200001040000700000300000";

        /*
         * "AI Escargot" by Arto Inkala
         *      Solved
         * 1 6 2 8 5 7 4 9 3
         * 5 3 4 1 2 9 6 7 8
         * 7 8 9 6 4 3 5 2 1
         * 4 7 5 3 1 2 9 8 6
         * 9 1 3 5 8 6 7 4 2
         * 6 2 8 7 9 4 1 3 5
         * 3 5 6 4 7 8 2 1 9
         * 2 4 1 9 3 5 8 6 7
         * 8 9 7 2 6 1 3 5 4
         */
//        final String field = "100007090030020008009600500005300900010080002600004000300000010040000007007000300";

        /*
         * "Inkara2012" by Arto Inkala
         *      Solved
         * 8 1 2 7 5 3 6 4 9
         * 9 4 3 6 8 2 1 7 5
         * 6 7 5 4 9 1 2 8 3
         * 1 5 4 2 3 7 8 9 6
         * 3 6 9 8 4 5 7 2 1
         * 2 8 7 1 6 9 5 3 4
         * 5 2 1 9 7 4 3 6 8
         * 4 3 8 5 2 6 9 1 7
         * 7 9 6 3 1 8 4 5 2
         */
//        final String field = "800000000003600000070090200050007000000045700000100030001000068008500010090000400";

        /*
         * "Platinum Blonde"
         *      Solved
         * 8 3 9 4 6 5 7 1 2
         * 1 4 6 7 8 2 9 5 3
         * 7 5 2 3 9 1 4 8 6
         * 3 9 1 8 2 4 6 7 5
         * 5 6 4 1 7 3 8 2 9
         * 2 8 7 6 5 9 3 4 1
         * 6 2 8 5 3 7 1 9 4
         * 9 1 3 2 4 8 5 6 7
         * 4 7 5 9 1 6 2 3 8
         */
//        final String field = "000000012000000003002300400001800005060070800000009000008500000900040500470006000";

        SudokuSolver solver = SudokuSolver.of(field)
                .withPrecision(1e-5)
                .withLogging(true)
                .build();
        IntStream.range(0, getRuntime().availableProcessors()).parallel().forEach(i -> {
            Field result = solver.solve();

            // printing out the answer
            StringBuilder out = new StringBuilder(171);
            for (Row row : result.rows()) {
                for (Cell cell : row.cells()) {
                    out.append(' ');
                    out.append(cell.value());
                }
                out.append('\n');
            }
            synchronized (Main.class) {
                System.out.print(out);
                System.out.flush();
                System.exit(0);
            }
        });
    }
}
