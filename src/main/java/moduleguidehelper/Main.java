package moduleguidehelper;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class Main {

    public static final String ELECTIVE = "Wahlpflicht";

    public static final Gson GSON = new Gson();

    public static String lineSeparator = "\n";

    public static void main(final String[] args) throws IOException {
        if (args != null && args.length == 1) {
            final String singeModules = "singlepdfs";
            final File singleModulesDirectory = new File(singeModules);
            if (!singleModulesDirectory.exists()) {
                singleModulesDirectory.mkdir();
            }
            final File modules = new File(args[0]);
            for (final File json : modules.listFiles()) {
                if ("schema.json".equals(json.getName())) {
                    continue;
                }
                final String id = json.getName().substring(0, json.getName().length() - 5);
                final RawModule module;
                try (FileReader moduleReader = new FileReader(json)) {
                    module = Main.GSON.fromJson(moduleReader, RawModule.class);
                } catch (final MalformedJsonException | JsonSyntaxException e) {
                    System.out.println(json.getAbsolutePath());
                    throw e;
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(singeModules + "/" + id + ".tex"))) {
                    ModuleGuideLaTeXWriter.writeModule(id.toUpperCase(), module, 180, args[0], writer);
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
            System.out.println("Call with guide JSON, modules folder, and output file!");
            return;
        }
        final ModuleGuide guide = Main.parseModuleGuide(args[0], args[1]);
        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]))
        ) {
            new ModuleGuideLaTeXWriter(guide).write(args[1], writer);
        }
    }

    public static void newLine(final BufferedWriter writer) throws IOException {
        writer.write(Main.lineSeparator);
    }

    private static ModuleGuide parseModuleGuide(final String guide, final String modulesFolder) throws IOException {
        final MetaModuleGuide metaGuide;
        try (FileReader guideReader = new FileReader(guide)) {
            metaGuide = Main.GSON.fromJson(guideReader, MetaModuleGuide.class);
        }
        final List<Module> modules = new ArrayList<Module>();
        for (final MetaModule meta : metaGuide.modules()) {
            final File moduleJson = new File(modulesFolder + "/" + meta.module().toLowerCase() + ".json");
            if (!moduleJson.exists()) {
                System.out.println(meta.module());
                continue;
            }
            try (FileReader moduleReader = new FileReader(moduleJson)) {
                modules.add(new Module(meta, Main.GSON.fromJson(moduleReader, RawModule.class)));
            }
        }
        return new ModuleGuide(
            metaGuide.subject(),
            metaGuide.degree(),
            metaGuide.timemodel(),
            metaGuide.year(),
            metaGuide.generallanguage(),
            metaGuide.pagebreaks(),
            metaGuide.pagebreaksspecialization(),
            metaGuide.signature(),
            modules
        );
    }

}
