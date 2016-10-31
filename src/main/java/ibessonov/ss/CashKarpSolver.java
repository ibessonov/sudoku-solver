package ibessonov.ss;

import java.util.Optional;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static ibessonov.ss.Util.log;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.stream.IntStream.range;

/**
 * @author ibessonov
 */
final class CashKarpSolver {

    private static final class Coefficient {
        final int index, c;

        Coefficient(int index, int c) {
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

    private static final double[] dividers = { 1, 5, 40, 10, 54, 110592 };
    private static final double[] b5 = { 37d/378,     0, 250d/621,     125d/594,     0,          512d/1771 };
    private static final double[] b4 = { 2825d/27648, 0, 18575d/48384, 13525d/55296, 277d/14336, 1d/4      };

    private final int N, M, L; // L is N + M
    private final double precision;
    private final boolean loggingEnabled;
    private final Coefficient[][] d1; // sparse matrix of disjuncts
    private final Coefficient[][] d2; // transposed sparse matrix of disjuncts
    private final long[] km; // normalization for Km and Kmi methods
    private final double[] y;

    public CashKarpSolver(int N, PositiveLiteral[][] disjunctions, double precision, boolean loggingEnabled) {
        this.N = N;
        this.M = disjunctions.length;
        this.L = this.N + this.M;
        this.precision = precision;
        this.loggingEnabled = loggingEnabled;
        this.y = new double[L];
        this.d1 = Stream.of(disjunctions).map(d
                -> Stream.of(d).map(v -> new Coefficient(v.ijx, v.present ? 1 : -1)).toArray(Coefficient[]::new)
        ).toArray(Coefficient[][]::new);
        this.d2 = range(0, N).mapToObj(i
                -> range(0, M).mapToObj(m
                        -> Stream.of(d1[m])
                                 .filter(pair -> pair.index == i)
                                 .findFirst()
                                 .map(pair -> new Coefficient(m, pair.c))
                ).filter(Optional::isPresent)
                .map(Optional::get).toArray(Coefficient[]::new)
        ).toArray(Coefficient[][]::new);
        this.km = Stream.of(disjunctions).mapToLong(d -> 1L << d.length).toArray();
    }

    private static double randomDouble(Random random) {
        return random.nextDouble() * 0.9999998 + 0.0000001; // avoid 0 and 1
    }

    public int[] solve() {
        double dt = 0.03125, t = 0, dmax = 1;

        Random random = new Random();
        range(0, N).forEach(i -> y[i] = 2 * randomDouble(random) - 1);
        range(N, L).forEach(i -> y[i] = randomDouble(random));
        Stream.of(d1).filter(l -> l.length == 1).forEach(l -> y[l[0].index] = l[0].c); // cheap optimization, works sometimes

        double[] _y = new double[L];
        double[][] k = new double[butcherTableau.length][L];

        arraycopy(y, 0, _y, 0, L);
        int outCount = 0; // logging stuff

        while (dmax > 0.1) {
            for (int b = 0; b < butcherTableau.length; b++) {
                {
                    double butcherTableau_b[] = butcherTableau[b], divider = dividers[b];
                    for (int l = 0; l < L; l++) {
                        double y_l = 0;
                        for (int j = 0; j < b; j++) y_l += butcherTableau_b[j] * k[j][l] / divider;
                        y[l] = _y[l] + dt * y_l;
                    }
                }

                double[] k_b = k[b];
                for (int i = 0; i < N; i++) k_b[i] = ds(i);
                for (int m = 0; m < M; m++) k_b[N + m] = da(m);
            }

            double tau = 0;
            for (int l = 0; l < L; l++) {
                double err = 0, y_l = 0;
                for (int j = 0; j < b5.length; j++) {
                    double k_jl = k[j][l];
                    err += b4[j] * k_jl;
                    y_l += b5[j] * k_jl;
                }
                err -= y_l;
                if (err < 0) err = -err;
                err = _y[l] == 0d ? 0 : err / _y[l];
                if (err > tau) tau = err;
                y[l] = _y[l] + dt * y_l;
            }
            tau *= dt;
            dt *= 0.9 * min(max(precision / tau, 0.3), 2); // right from wikipedia!

            if (/*dt > 1e-6 && */tau > precision) {
                outCount = (outCount + 1) % 1000;
                if (outCount == 0) {
                    if (loggingEnabled) {
                        //noinspection OptionalGetWithoutIsPresent
                        double max = DoubleStream.of(_y).skip(N).max().getAsDouble();
                        log("%.4f\t%.4f\t%.4f\n", t, dmax, max);
                    }
                }
                continue;
            }

            t += dt;

            dmax = 0;
            for (int m = 0; m < M; m++) {
                double d = Km(m);
                if (d > dmax) dmax = d;
            }

             arraycopy(y, 0, _y, 0, L);
        }
        if (loggingEnabled) {
            log("%.4f\n", t);
        }
        return range(0, N).filter(i -> y[i] > 0).toArray();
    }

    private double Km(int m) {
        double result = 1;
        for (Coefficient pair : d1[m]) {
            result *= (1 - y[pair.index] * pair.c);
        }
        return result / km[m];
    }

    private double Kmi(int m, int i) {
        double result = 1;
        for (Coefficient pair : d1[m]) {
            if (pair.index == i) {
                continue;
            }
            result *= (1 - y[pair.index] * pair.c);
        }
        return result / km[m];
    }

    // ds/dt
    private double ds(int i) {
        double result = 0;
        for (Coefficient pair : d2[i]) {
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