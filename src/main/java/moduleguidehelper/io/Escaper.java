package moduleguidehelper.io;

@FunctionalInterface
public interface Escaper {

    String escape(int from, int to, String text);

}
