package moduleguidehelper.model.bibtex;

import java.io.*;

import moduleguidehelper.io.*;

public record BibTeXComment(String comment) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        return 0;
    }

    @Override
    public void format(final BibTeXFormatter formatter, final Writer writer) throws IOException {
        formatter.format(this, writer);
    }

}
