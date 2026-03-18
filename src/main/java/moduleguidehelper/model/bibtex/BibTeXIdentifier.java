package moduleguidehelper.model.bibtex;

public record BibTeXIdentifier(String identifier) implements BibTeXValue {

    @Override
    public String toFormatString(final boolean brace) {
        return this.identifier();
    }

}
