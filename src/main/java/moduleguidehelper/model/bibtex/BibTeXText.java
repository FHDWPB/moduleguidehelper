package moduleguidehelper.model.bibtex;

public record BibTeXText(String text) implements BibTeXValue {

    @Override
    public String toFormatString(final boolean brace) {
        return String.format(brace ? "{%s}" : "\"%s\"", this.text());
    }

}
