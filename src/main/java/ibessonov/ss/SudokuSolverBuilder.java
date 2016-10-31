package ibessonov.ss;

/**
 * @author ibessonov
 */
public final class SudokuSolverBuilder {

    @FunctionalInterface
    private interface Configurator<T> {

        void configure(T t);

        default T apply(T t) {
            configure(t);
            return t;
        }

        default Configurator<T> combine(Configurator<? super T> then) {
            return t -> then.configure(apply(t));
        }
    }

    private final String sudoku;
    private final Configurator<SudokuSolver> configurator;

    public SudokuSolverBuilder(String sudoku) {
        this(sudoku, s -> {});
    }

    private SudokuSolverBuilder(String sudoku, Configurator<SudokuSolver> configurator) {
        this.sudoku = sudoku;
        this.configurator = configurator;
    }

    public SudokuSolverBuilder withPrecision(double precision) {
        return new SudokuSolverBuilder(sudoku, configurator.combine(s -> s.setPrecision(precision)));
    }

    public SudokuSolverBuilder withLogging(boolean enable) {
        return new SudokuSolverBuilder(sudoku, configurator.combine(s -> s.enableLogging(enable)));
    }

    public SudokuSolver build() {
        SudokuSolver solver = configurator.apply(new SudokuSolver(sudoku));
        solver.init();
        return solver;
    }
}
