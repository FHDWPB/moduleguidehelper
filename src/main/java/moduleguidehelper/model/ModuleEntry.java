package moduleguidehelper.model;

public record ModuleEntry(String id, String title) implements Comparable<ModuleEntry> {

    @Override
    public int compareTo(final ModuleEntry o) {
        return this.id().compareTo(o.id());
    }

}
