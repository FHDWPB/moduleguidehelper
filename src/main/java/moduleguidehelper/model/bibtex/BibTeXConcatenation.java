package moduleguidehelper.model.bibtex;

public record BibTeXConcatenation(BibTeXValue left, BibTeXValue right) implements BibTeXValue {

    @Override
    public String toFormatString(final boolean brace) {
        return String.format("%s # %s", this.left().toFormatString(false), this.right().toFormatString(false));
    }

}
