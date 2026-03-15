package moduleguidehelper.model.bibtex;

public record BibTeXComment(String comment) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        return 0;
    }

}
