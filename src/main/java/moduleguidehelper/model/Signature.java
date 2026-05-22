package moduleguidehelper.model;

import java.io.*;

import moduleguidehelper.*;
import moduleguidehelper.internationalization.*;
import moduleguidehelper.io.*;

public enum Signature {

    ANGELIKA((internationalization, writer) -> {
        writer.write("Prof. Dr. Angelika Röchter\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.DEAN_BA));
    }),

    GREGOR((internationalization, writer) -> {
        writer.write("Prof. Dr. Gregor Sandhaus\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.DEAN_CS));
    }),

    THOMAS((internationalization, writer) -> {
        writer.write("Thomas Mertens\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.COURSE_DIRECTOR));
        writer.write(" ");
        writer.write(internationalization.internationalize(InternationalizationKey.MBA));
    });

    public final CheckedBiConsumer<Internationalization, BufferedWriter, IOException> toLaTeX;

    private Signature(final CheckedBiConsumer<Internationalization, BufferedWriter, IOException> toLaTeX) {
        this.toLaTeX = toLaTeX;
    }

}
