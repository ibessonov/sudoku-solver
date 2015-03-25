package ibessonov;

import ibessonov.ss.Field;
import ibessonov.ss.Field.Cell;
import ibessonov.ss.Field.Row;
import ibessonov.ss.SudokuSolver;

/**
 *
 * @author ibessonov
 */
public class Main {

    public static void main(String[] args) {
//        final String src = "005300000800000020070010500400005300010070006003200080060500009004000030000009700";
// 1 4 5 3 2 7 6 9 8
// 8 3 9 6 5 4 1 2 7
// 6 7 2 9 1 8 5 4 3
// 4 9 6 1 8 5 3 7 2
// 2 1 8 4 7 3 9 5 6
// 7 5 3 2 9 6 4 8 1
// 3 6 7 5 4 2 8 1 9
// 9 8 4 7 6 1 2 3 5
// 5 2 1 8 3 9 7 6 4
//        final String src = "800000000003600000070090200050007000000045700000100030001000068008500010090000400";
// 8 1 2 7 5 3 6 4 9
// 9 4 3 6 8 2 1 7 5
// 6 7 5 4 9 1 2 8 3
// 1 5 4 2 3 7 8 9 6
// 3 6 9 8 4 5 7 2 1
// 2 8 7 1 6 9 5 3 4
// 5 2 1 9 7 4 3 6 8
// 4 3 8 5 2 6 9 1 7
// 7 9 6 3 1 8 4 5 2
        final String src = "000200063300005404001003980000000090000538000030000000026300500503700008470001000";
//        final String src = "000000012" + "000000003" + "002300400" + "001800005" + "060070800" + "000009000" + "008500000" + "900040500" + "470006000";
//        final String src = "370600000009000000060020180000005000020010090000400000016090070000000500000007042";
// 3 7 2 6 8 1 9 5 4
// 1 8 9 5 4 3 2 6 7
// 4 6 5 7 2 9 1 8 3
// 7 3 8 9 6 5 4 2 1
// 6 2 4 3 1 8 7 9 5
// 9 5 1 4 7 2 8 3 6
// 5 1 6 2 9 4 3 7 8
// 2 4 7 8 3 6 5 1 9
// 8 9 3 1 5 7 6 4 2
        
        Field result = SudokuSolver.builder(src)
                .withPrecision(1e-5)
                .enableLogging(true)
                .build()
                .solve();

        // printing out the answer
        for (Row row : result.rows()) {
            for (Cell cell : row.cells()) {
                System.out.print(' ');
                System.out.print(cell.value());
            }
            System.out.println();
        }
    }
}
