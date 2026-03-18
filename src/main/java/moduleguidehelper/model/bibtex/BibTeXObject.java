package moduleguidehelper.model.bibtex;

import java.io.*;

import moduleguidehelper.io.*;

public interface BibTeXObject extends Comparable<BibTeXObject> {

    void format(BibTeXFormatter formatter, Writer writer) throws IOException;

}
