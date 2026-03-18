package moduleguidehelper.model.bibtex;

import java.io.*;

import moduleguidehelper.io.*;

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
            return this.identifier().toLowerCase().compareTo(s.identifier().toLowerCase());
        }
        return -1;
    }

    @Override
    public void format(final BibTeXFormatter formatter, final Writer writer) throws IOException {
        formatter.format(this, writer);
    }

}
