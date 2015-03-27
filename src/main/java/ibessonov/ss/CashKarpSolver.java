package ibessonov.ss;

import static ibessonov.ss.Util.log;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import static java.util.stream.IntStream.range;
import java.util.stream.Stream;

/**
 *
 * @author ibessonov
 */
final class CashKarpSolver {

    private static final class Literal {
        public final int index, c;

        public Literal(int index, int c) {
            this.index = index;
            this.c = c;
        }
    }

    private static final double[][] butcherTableau = {
        { 0,    0,     0,    0,     0    },
        { 1,    0,     0,    0,     0    },
        { 3,    9,     0,    0,     0    },
        { 3,   -9,     12,   0,     0    },
        {-11,   135,  -140,  70,    0    },
        { 3262, 37800, 4600, 44275, 6831 },
    };

    private static final double[] deviders = { 1, 5, 40, 10, 54, 110592 };
    private static final double[] b5 = { 37d/378,     0, 250d/621,     125d/594,     0,          512d/1771 };
    private static final double[] b4 = { 2825d/27648, 0, 18575d/48384, 13525d/55296, 277d/14336, 1d/4      };

    private final int N, M, L;
    private final double precision;
    private final boolean loggingEnabled;
    private final Literal[][] d1;
    private final Literal[][] d2;
    private final int[] km;
    private final double[] y;
    private final Random random = new Random();

    public CashKarpSolver(int N, PositiveLiteral[][] disjunctions, double precision, boolean loggingEnabled) {
        this.N = N;
        this.M = disjunctions.length;
        this.L = this.N + this.M;
        this.precision = precision;
        this.loggingEnabled = loggingEnabled;
        this.y = new double[L];
        this.d1 = Stream.of(disjunctions).map(d
                -> Stream.of(d).map(v -> new Literal(v.index, v.present ? 1 : -1)).toArray(Literal[]::new)
        ).toArray(Literal[][]::new);
        this.d2 = range(0, N).mapToObj(i
                -> range(0, M).mapToObj(m
                        -> Stream.of(d1[m])
                                 .filter(pair -> pair.index == i)
                                 .findFirst()
                                 .map(pair -> new Literal(m, pair.c))
                ).filter(Optional<Literal>::isPresent)
                .map(Optional<Literal>::get).toArray(Literal[]::new)
        ).toArray(Literal[][]::new);
        this.km = Stream.of(disjunctions).mapToInt(d -> d.length).toArray();
    }

    public int[] solve() {
        double dt = 0.03125, t = 0, dmax = 1;

        range(0, N).forEach(i -> y[i] = 2 * random.nextDouble() - 1);
        range(N, L).forEach(i -> y[i] = 1 + Math.abs(random.nextGaussian()));
        Stream.of(d1).filter(l -> l.length == 1).forEach(l -> y[l[0].index] = l[0].c);

        double[] _y = new double[L];
        double[][] k = new double[butcherTableau.length][L];

        System.arraycopy(y, 0, _y, 0, L);
        int outCount = 0; // logging stuff

        while (dmax > 0.1) {
            for (int b = 0; b < butcherTableau.length; b++) {
                Arrays.fill(y, 0);

                for (int l = 0; l < L; l++)
                    for (int j = 0; j < b; j++) y[l] += butcherTableau[b][j] * k[j][l] / deviders[b];

                for (int l = 0; l < L; l++) y[l] = _y[l] + dt * y[l];

                for (int i = 0; i < N; i++) k[b][i] = ds(i);
                for (int m = 0; m < M; m++) k[b][N + m] = da(m);
            }

            Arrays.fill(y, 0);

            for (int l = 0; l < L; l++) {
                for (int j = 0; j < b5.length; j++) y[l] += (b5[j] - b4[j]) * k[j][l];
                y[l] *= dt;
            }

            double error = 0;
            for (int i = 0; i < N; i++) {
                double e = Math.abs(y[i]);
                if (e > error) error = e;
            }

            if (error < precision / 100) {
                dt /= 0.75;
                System.arraycopy(_y, 0, y, 0, L);
                continue;
            } else if (error > precision) {
                dt *= 0.75;
                if (outCount == 0 && loggingEnabled) {
                    log("%.4f %.4f\n", t, dmax);
                }
                outCount = (outCount + 1) % 100;
                System.arraycopy(_y, 0, y, 0, L);
                continue;
            }

            Arrays.fill(y, 0);

            for (int l = 0; l < L; l++)
                for (int j = 0; j < b5.length; j++) y[l] += b5[j] * k[j][l];

            for (int l = 0; l < L; l++) y[l] = _y[l] + dt * y[l];

            t += dt;

            dmax = 0;
            for (int m = 0; m < M; m++) {
                double d = Km(m);
                if (d > dmax) dmax = d;
            }

            System.arraycopy(y, 0, _y, 0, L);
        }
        if (loggingEnabled) {
            log("%.4f\n", t);
        }
        return range(0, N).filter(i -> y[i] > 0).toArray();
    }

    private double Km(int m) {
        double result = 1;
        for (Literal pair : d1[m]) {
            result *= (1 - y[pair.index] * pair.c);
        }
        return result / (1L << km[m]);
    }

    private double Kmi(int m, int i) {
        double result = 1;
        for (Literal pair : d1[m]) {
            if (pair.index == i) {
                continue;
            }
            result *= (1 - y[pair.index] * pair.c);
        }
        return result / (1L << km[m]);
    }

    // ds/dt
    private double ds(int i) {
        double result = 0;
        for (Literal pair : d2[i]) {
            int m = pair.index;
            double kmi = Kmi(m, i);
            result += y[N + m] * pair.c * kmi * kmi * (1 - y[i] * pair.c);
        }
        return result + result;
    }

    // da/dt
    private double da(int m) {
        return y[N + m] * Km(m);
    }
}