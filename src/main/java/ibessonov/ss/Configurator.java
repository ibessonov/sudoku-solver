package ibessonov.ss;

/**
 *
 * @author ibessonov
 */
@FunctionalInterface
interface Configurator<T> {

    void configure(T t);

    default T accept(T t) {
        configure(t);
        return t;
    }

    default Configurator<T> andThen(Configurator<? super T> after) {
        return t -> { configure(t); after.configure(t); };
    }
}
