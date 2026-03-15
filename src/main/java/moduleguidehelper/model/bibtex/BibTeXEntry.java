package moduleguidehelper.model.bibtex;

import java.util.*;

public record BibTeXEntry(String type, String identifier, Map<String, BibTeXValue> tags) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        if (o instanceof final BibTeXEntry e) {
            return this.identifier().compareTo(e.identifier());
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

}
