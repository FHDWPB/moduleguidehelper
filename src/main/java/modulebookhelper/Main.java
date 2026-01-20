package modulebookhelper;

import java.io.*;

import com.google.gson.*;

public class Main {

    private static final Gson GSON = new Gson();

    private static String lineSeparator = System.lineSeparator();

    public static void main(final String[] args) throws IOException {
        try (
            FileReader bookReader = new FileReader(args[0]);
            FileReader modulesReader = new FileReader(args[1]);
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]))
        ) {
            final ModuleBook book = Main.GSON.fromJson(bookReader, ModuleBook.class);
            final ModuleMap modules = Main.GSON.fromJson(modulesReader, ModuleMap.class);
            new ModuleBookLaTeXWriter(book, modules).write(writer);
        }
    }

    public static void newLine(final BufferedWriter writer) throws IOException {
        writer.write(Main.lineSeparator);
    }

}
