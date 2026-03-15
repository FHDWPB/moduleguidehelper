package moduleguidehelper.model.bibtex;

public record BibTeXPreamble(String comment) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        if (o instanceof BibTeXPreamble) {
            return 0;
        }
        if (o instanceof BibTeXComment) {
            return 0;
        }
        if (o instanceof BibTeXFreeComment) {
            return 0;
        }
        return -1;
    }

}
