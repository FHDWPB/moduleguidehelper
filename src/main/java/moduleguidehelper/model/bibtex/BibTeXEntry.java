package moduleguidehelper.model.bibtex;

import java.io.*;
import java.util.*;

import moduleguidehelper.io.*;

public record BibTeXEntry(String type, String identifier, Map<String, BibTeXValue> tags) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        if (o instanceof final BibTeXEntry e) {
            return this.identifier().toLowerCase().compareTo(e.identifier().toLowerCase());
        }
        if (o instanceof final BibTeXCompoundObject c) {
            return -c.compareTo(this);
        }
        if (o instanceof BibTeXPreamble) {
            return 1;
        }
        if (o instanceof BibTeXComment) {
            return 0;
        }
        if (o instanceof BibTeXFreeComment) {
            return 0;
        }
        if (o instanceof BibTeXString) {
            return 1;
        }
        return 0;
    }

    @Override
    public void format(final BibTeXFormatter formatter, final Writer writer) throws IOException {
        formatter.format(this,  writer);
    }

}
