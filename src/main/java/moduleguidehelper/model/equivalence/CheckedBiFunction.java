package moduleguidehelper.model.equivalence;

@FunctionalInterface
public interface CheckedBiFunction<A, B, C, E extends Exception> {

    C apply(A input1, B input2) throws E;

}
