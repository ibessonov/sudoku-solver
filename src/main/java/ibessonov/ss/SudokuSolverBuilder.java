package ibessonov.ss;

import java.util.function.Consumer;

/**
 *
 * @author ibessonov
 */
public final class SudokuSolverBuilder {

    private final String sudoku;
    private Consumer<SudokuSolver> configurator;

    public SudokuSolverBuilder(String sudoku) {
        this.sudoku = sudoku;
        this.configurator = s -> {};
    }

    public SudokuSolverBuilder withPrecision(double precision) {
        configurator = configurator.andThen(s -> s.setPrecision(precision));
        return this;
    }

    public SudokuSolverBuilder enableLogging(boolean enable) {
        configurator = configurator.andThen(s -> s.enableLogging(enable));
        return this;
    }

    public SudokuSolver build() {
        SudokuSolver result = new SudokuSolver(sudoku);
        configurator.accept(result);
        return result;
    }
}
