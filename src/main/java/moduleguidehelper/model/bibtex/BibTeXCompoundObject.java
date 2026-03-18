package moduleguidehelper.model.bibtex;

import java.io.*;
import java.util.*;

import moduleguidehelper.io.*;

public record BibTeXCompoundObject(List<BibTeXObject> comments, BibTeXObject object) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        return this.object().compareTo(o);
    }

    @Override
    public void format(final BibTeXFormatter formatter, final Writer writer) throws IOException {
        formatter.format(this,  writer);
    }

}
