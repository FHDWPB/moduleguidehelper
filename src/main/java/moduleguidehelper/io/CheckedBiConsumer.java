package moduleguidehelper.io;

@FunctionalInterface
public interface CheckedBiConsumer<A, B, E extends Exception> {

    void apply(A a, B b) throws E;

}
