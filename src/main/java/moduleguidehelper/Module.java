package moduleguidehelper;

public record Module(MetaModule meta, RawModule module) implements Comparable<Module> {

    @Override
    public int compareTo(final Module o) {
        return this.meta().compareTo(o.meta());
    }

}
