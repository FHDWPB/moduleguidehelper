package moduleguidehelper.model.equivalence;

public record Source(String source, String pages) {

    @Override
    public String toString() {
        final String result = String.format("\\cite{%s}", this.source());
        if (this.pages() != null && !this.pages().isBlank()) {
            return String.format("%s, %s", result, this.pages());
        }
        return result;
    }

}
