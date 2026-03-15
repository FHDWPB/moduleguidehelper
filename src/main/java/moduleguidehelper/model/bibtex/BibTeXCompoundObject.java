package moduleguidehelper.model.bibtex;

import java.util.*;

public record BibTeXCompoundObject(List<BibTeXObject> comments, BibTeXObject object) implements BibTeXObject {

    @Override
    public int compareTo(final BibTeXObject o) {
        return this.object().compareTo(o);
    }

}
