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

        final String src = "000200063300005404001003980000000090000538000030000000026300500503700008470001000";
//        final String src = "123456780456780123780123456234567801567801234801234567345678012678012345012345678";        
//        final String src = "000000012" + "000000003" + "002300400" + "001800005" + "060070800" + "000009000" + "008500000" + "900040500" + "470006000";

        Field result = SudokuSolver.of(src)
                .withPrecision(1e-5)
                .withLogging(true)
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
