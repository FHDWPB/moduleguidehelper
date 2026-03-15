package moduleguidehelper.model.bibtex;

public record BibTeXString(String identifier, BibTeXValue value) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        if (o instanceof BibTeXPreamble) {
            return 1;
        }
        if (o instanceof BibTeXComment) {
            return 0;
        }
        if (o instanceof BibTeXFreeComment) {
            return 0;
        }
        if (o instanceof final BibTeXString s) {
            return this.identifier().compareTo(s.identifier());
        }
        return -1;
    }

}
