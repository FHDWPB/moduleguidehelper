package moduleguidehelper.model.bibtex;

import java.math.*;

public record BibTeXNumber(BigInteger number) implements BibTeXValue {

    @Override
    public String toFormatString(final boolean brace) {
        return String.format(brace ? "{%s}" : "\"%s\"", this.number().toString());
    }

}
