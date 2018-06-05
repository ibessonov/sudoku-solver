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
