package moduleguidehelper.model;

import java.io.*;

import moduleguidehelper.*;
import moduleguidehelper.internationalization.*;
import moduleguidehelper.io.*;

public enum Signature {

    ANGELIKA((internationalization, writer) -> {
        writer.write("\\includegraphics{signature_angelika.jpg}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Prof. Dr. Angelika Röchter\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.DEAN_BA));
    }),

    GREGOR((internationalization, writer) -> {
        writer.write("\\includegraphics{signature_gregor.png}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Prof. Dr. Gregor Sandhaus\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.DEAN_CS));
    }),

    THOMAS((internationalization, writer) -> {
        writer.write("Thomas Mertens\\\\");
    });

    public final CheckedBiConsumer<Internationalization, BufferedWriter, IOException> toLaTeX;

    private Signature(final CheckedBiConsumer<Internationalization, BufferedWriter, IOException> toLaTeX) {
        this.toLaTeX = toLaTeX;
    }

}
