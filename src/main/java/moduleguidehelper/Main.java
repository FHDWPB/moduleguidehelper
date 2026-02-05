package moduleguidehelper;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class Main {

    public static String lineSeparator = System.lineSeparator();

    private static final Gson GSON = new Gson();

    public static void main(final String[] args) throws IOException {
        if (args != null && args.length == 1) {
            try (FileReader modulesReader = new FileReader(args[0])) {
                final ModuleMap modules = Main.GSON.fromJson(modulesReader, ModuleMap.class);
                final String singeModules = "singlemodules";
                final File singleModulesDirectory = new File(singeModules);
                if (!singleModulesDirectory.exists()) {
                    singleModulesDirectory.mkdir();
                }
                for (final Map.Entry<String, Module> entry : modules.entrySet()) {
                    if (entry.getKey().isBlank()) {
                        continue;
                    }
                    try (
                        BufferedWriter writer =
                            new BufferedWriter(
                                new FileWriter(singeModules + "/" + entry.getKey().toLowerCase() + ".tex")
                            )
                    ) {
                        ModuleGuideLaTeXWriter.writeModule(entry.getKey(), entry.getValue(), writer);
                    }
                }
            }
            return;
        }
        if (args != null && args.length == 2) {
            try (
                BufferedReader reader = new BufferedReader(new FileReader(args[0]));
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))
            ) {
                String line = reader.readLine();
                boolean contents = false;
                boolean sections = false;
                while (line != null) {
                    if ("inhalte".equals(line.trim().toLowerCase())) {
                        contents = true;
                    } else if ("grundlegende literaturhinweise".equals(line.trim().toLowerCase())) {
                        contents = false;
                    } else if ("literatur".equals(line.trim().toLowerCase())) {
                        contents = false;
                    } else if (contents) {
                        if (!line.replaceAll("•", "").isBlank() && !line.startsWith("Seite")) {
                            if (line.startsWith("o")) {
                                if (!sections) {
                                    sections = true;
                                    writer.write(",\n        \"sections\": [\n");
                                } else {
                                    writer.write(",\n");
                                }
                                writer.write("            \"");
                                writer.write(line.substring(1).trim());
                                writer.write("\"");
                            } else {
                                if (sections) {
                                    writer.write("\n        ]");
                                    sections = false;
                                }
                                writer.write("\n    },\n    {\n        \"chapter\": \"");
                                writer.write(line.trim());
                                writer.write("\"");
                            }
                        }
                    }
                    line = reader.readLine();
                }
            }
            return;
        }
        if (args == null || args.length != 3) {
            System.out.println("Call with book and modules JSON, followed by output file!");
            return;
        }
        try (
            FileReader bookReader = new FileReader(args[0]);
            FileReader modulesReader = new FileReader(args[1]);
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]))
        ) {
            final ModuleGuide guide = Main.GSON.fromJson(bookReader, ModuleGuide.class);
            final ModuleMap modules = Main.GSON.fromJson(modulesReader, ModuleMap.class);
            new ModuleGuideLaTeXWriter(guide, modules).write(writer);
        }
    }

    public static void newLine(final BufferedWriter writer) throws IOException {
        writer.write(Main.lineSeparator);
    }

}
