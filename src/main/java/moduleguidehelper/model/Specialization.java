package moduleguidehelper.model;

public record Specialization(String name, int order) implements Comparable<Specialization> {

    @Override
    public int compareTo(final Specialization o) {
        if (this.order() == o.order()) {
            return this.name().compareTo(o.name());
        }
        return Integer.compare(this.order(), o.order());
    }

}
