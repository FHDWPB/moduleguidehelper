package moduleguidehelper.model.bibtex;

import java.util.*;

public class BibTeXDatabase extends ArrayList<BibTeXObject> {

    private static final long serialVersionUID = 1L;

    public BibTeXDatabase() {
        super();
    }

    public BibTeXDatabase(final Collection<? extends BibTeXObject> c) {
        super(c);
    }

}
