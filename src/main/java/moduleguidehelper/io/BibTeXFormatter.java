package moduleguidehelper.io;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import moduleguidehelper.model.bibtex.*;

public class BibTeXFormatter {

    private final BibTeXIdentifierFormat commandFormat;

    private String indent;

    private boolean sort;

    private final BibTeXIdentifierFormat tagIdentifierFormat;

    private final boolean trailingComma;

    public BibTeXFormatter() {
        this.indent = "  ";
        this.sort = true;
        this.trailingComma = false;
        this.commandFormat = BibTeXIdentifierFormat.LOWER_CASE;
        this.tagIdentifierFormat = BibTeXIdentifierFormat.LOWER_CASE;
    }

    public void format(final BibTeXComment comment, final Writer writer) throws IOException {
        writer.write("@");
        switch (this.commandFormat) {
        case LOWER_CASE:
            writer.write("comment");
            break;
        case UPPER_CASE:
            writer.write("COMMENT");
            break;
        default:
            throw new IllegalStateException("Unknown enum constant!");
        }
        writer.write("{");
        writer.write(comment.comment());
        writer.write("}\n\n");
    }

    public void format(final BibTeXCompoundObject compoundObject, final Writer writer) throws IOException {
        for (final BibTeXObject comment : compoundObject.comments()) {
            comment.format(this, writer);
        }
        compoundObject.object().format(this, writer);
    }

    public void format(final BibTeXDatabase db, final Writer writer) throws IOException {
        final List<BibTeXObject> objects = this.getObjects(db);
        for (final BibTeXObject object : objects) {
            object.format(this, writer);
        }
    }

    public void format(final BibTeXEntry entry, final Writer writer) throws IOException {
        writer.write("@");
        switch (this.commandFormat) {
        case LOWER_CASE:
            writer.write(entry.type().toLowerCase());
            break;
        case UPPER_CASE:
            writer.write(entry.type().toUpperCase());
            break;
        default:
            throw new IllegalStateException("Unknown enum constant!");
        }
        writer.write("{");
        writer.write(entry.identifier());
        for (final Entry<String, BibTeXValue> tag : entry.tags().entrySet()) {
            writer.write(",\n");
            writer.write(this.indent);
            switch (this.tagIdentifierFormat) {
            case LOWER_CASE:
                writer.write(tag.getKey().toLowerCase());
                break;
            case UPPER_CASE:
                writer.write(tag.getKey().toUpperCase());
                break;
            default:
                throw new IllegalStateException("Unknown enum constant!");
            }
            writer.write(" = ");
            writer.write(tag.getValue().toFormatString(true));
        }
        if (this.trailingComma) {
            writer.write(",");
        }
        writer.write("\n}\n\n");
    }

    public void format(final BibTeXFreeComment freeComment, final Writer writer) throws IOException {
        writer.write(freeComment.comment());
    }

    public void format(final BibTeXPreamble preamble, final Writer writer) throws IOException {
        writer.write("@");
        switch (this.commandFormat) {
        case LOWER_CASE:
            writer.write("preamble");
            break;
        case UPPER_CASE:
            writer.write("PREAMBLE");
            break;
        default:
            throw new IllegalStateException("Unknown enum constant!");
        }
        writer.write("{");
        writer.write(preamble.content());
        writer.write("}\n\n");
    }

    public void format(final BibTeXString bibString, final Writer writer) throws IOException {
        writer.write("@");
        switch (this.commandFormat) {
        case LOWER_CASE:
            writer.write("string");
            break;
        case UPPER_CASE:
            writer.write("STRING");
            break;
        default:
            throw new IllegalStateException("Unknown enum constant!");
        }
        writer.write("{");
        writer.write(bibString.identifier());
        writer.write(" = ");
        writer.write(bibString.value().toFormatString(false));
        writer.write("}\n\n");
    }

    public BibTeXFormatter setIndent(final String indent) {
        this.indent = indent;
        return this;
    }

    public BibTeXFormatter setSort(final boolean sort) {
        this.sort = sort;
        return this;
    }

    private List<BibTeXObject> getObjects(final BibTeXDatabase db) {
        if (!this.sort) {
            return db;
        }
        final List<BibTeXObject> result = new ArrayList<BibTeXObject>();
        final Iterator<BibTeXObject> iterator = db.iterator();
        List<BibTeXObject> compound = new ArrayList<BibTeXObject>();
        while (iterator.hasNext()) {
            final BibTeXObject object = iterator.next();
            if (object instanceof final BibTeXFreeComment freeComment) {
                compound.add(freeComment);
            } else if (object instanceof final BibTeXComment comment) {
                compound.add(comment);
            } else if (compound.isEmpty()) {
                result.add(object);
            } else {
                result.add(new BibTeXCompoundObject(compound, object));
                compound = new ArrayList<BibTeXObject>();
            }
        }
        Collections.sort(result);
        return result;
    }

}
