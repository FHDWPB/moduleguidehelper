package moduleguidehelper;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

import com.google.gson.*;
import com.google.gson.stream.*;

import moduleguidehelper.model.*;
import moduleguidehelper.model.Module;
import moduleguidehelper.view.*;

public class Main {

    public static final String ELECTIVE = "Wahlpflicht";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static String lineSeparator = "\n";

    public static final Logger LOGGER = Logger.getLogger("moduleguidehelper");

    private static final String VERSION = "1.3.0";

    public static Process buildAndStartBiberProcess(final String fileName, final File directory) throws IOException {
        return new ProcessBuilder(
            "biber",
            fileName
        ).inheritIO().directory(directory).start();
    }

    public static Process buildAndStartPDFLaTeXProcess(final String fileName, final File directory) throws IOException {
        return new ProcessBuilder(
            "pdflatex",
            fileName,
            "-interaction=nonstopmode",
            "-halt-on-error"
        ).inheritIO().directory(directory).start();
    }

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length == 0) {
            new MainFrame(
                Main.VERSION,
                new File(System.getProperty("user.dir"))
            ).setVisible(true);
            return;
        }
        if (args != null && args.length == 1) {
            Main.LOGGER.setLevel(Level.FINE);
            final String singeModules = "singlepdfs";
            final File singleModulesDirectory = new File(singeModules);
            if (!singleModulesDirectory.exists()) {
                singleModulesDirectory.mkdir();
            }
            final File modules = new File(args[0]);
            for (final File json : modules.listFiles()) {
                final String id = json.getName().substring(0, json.getName().length() - 5);
                final RawModule module;
                try (FileReader moduleReader = new FileReader(json)) {
                    module = Main.GSON.fromJson(moduleReader, RawModule.class);
                } catch (final MalformedJsonException | JsonSyntaxException e) {
                    Main.LOGGER.log(Level.SEVERE, json.getAbsolutePath());
                    throw new IOException(String.format("%s: %s", json.getAbsolutePath(), e.getMessage()), e);
                }
                if ("schema.json".equals(json.getName())) {
                    Main.prettyPrint(json, module);
                    continue;
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(singeModules + "/" + id + ".tex"))) {
                    ModuleGuideLaTeXWriter.writeModule(id.toUpperCase(), module, 180, args[0], writer);
                }
                Main.prettyPrint(json, module);
            }
            return;
        }
        Main.LOGGER.setLevel(Level.SEVERE);
        if (args != null && args.length == 2) {
            final File modulesDirectory = new File(args[0]);
            final FileFilter fileFilter =
                file -> file.getName().endsWith(".json") && !file.getName().startsWith("schema");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {
                final List<Source> sources = new ArrayList<Source>();
                for (final File json : modulesDirectory.listFiles(fileFilter)) {
                    final RawModule module;
                    try (FileReader moduleReader = new FileReader(json)) {
                        module = Main.GSON.fromJson(moduleReader, RawModule.class);
                    } catch (final MalformedJsonException | JsonSyntaxException e) {
                        Main.LOGGER.log(Level.SEVERE, json.getAbsolutePath());
                        throw e;
                    }
                    if (module.requiredliterature() != null) {
                        sources.addAll(module.requiredliterature());
                    }
                    if (module.optionalliterature() != null) {
                        sources.addAll(module.optionalliterature());
                    }
                }
                Collections.sort(sources, (s1, s2) -> Main.toSourceId(s1).compareTo(Main.toSourceId(s2)));
                for (final Source source : sources) {
                    Main.writeSource(source, writer);
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

    private static String formatAuthor(final String author) {
        if (author.contains("|")) {
            final int index = author.indexOf('|');
            return String.format("%s, %s", author.substring(index + 1), author.substring(0, index));
        } else {
            final String[] nameParts = author.split(" ");
            return String.format(
                "%s, %s",
                nameParts[nameParts.length - 1],
                Arrays.stream(nameParts).limit(nameParts.length - 1).collect(Collectors.joining(" "))
            );
        }
    }

    private static ModuleGuide parseModuleGuide(final String guide, final String modulesFolder) throws IOException {
        final MetaModuleGuide metaGuide;
        try (FileReader guideReader = new FileReader(guide)) {
            metaGuide = Main.GSON.fromJson(guideReader, MetaModuleGuide.class);
        } catch (RuntimeException | IOException e) {
            throw new IOException(String.format("Exception on module guide %s: %s", guide, e.getMessage()), e);
        }
        final List<Module> modules = new ArrayList<Module>();
        for (final MetaModule meta : metaGuide.modules()) {
            final File moduleJson = new File(modulesFolder + "/" + meta.module().toLowerCase() + ".json");
            if (!moduleJson.exists()) {
                Main.LOGGER.log(Level.SEVERE, meta.module() + " is missing!");
                continue;
            }
            try (FileReader moduleReader = new FileReader(moduleJson)) {
                modules.add(new Module(meta, Main.GSON.fromJson(moduleReader, RawModule.class)));
            } catch (RuntimeException | IOException e) {
                throw new IOException(
                    String.format("Exception on module %s: %s", moduleJson.getPath(), e.getMessage()),
                    e
                );
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

    private static void prettyPrint(final File json, final RawModule module) throws IOException {
        try (JsonWriter writer = new JsonWriter(new FileWriter(json))) {
            writer.setIndent("    ");
            writer.setSerializeNulls(false);
            Main.GSON.toJson(module, RawModule.class, writer);
        }
    }

    private static String toAuthorEntry(final List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return "";
        }
        return authors.stream().map(Main::formatAuthor).collect(Collectors.joining(" and "));
    }

    private static String toPageEntry(final Integer frompage, final Integer topage) {
        if (frompage == null) {
            return "";
        }
        if (topage == null) {
            return String.valueOf(frompage);
        }
        return String.format("%d--%d", frompage, topage);
    }

    private static String toSourceEntry(final String key, final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return String.format("%s = {%s}", key, value);
    }

    private static String toSourceId(final Source source) {
        if (source.authors() == null || source.authors().isEmpty()) {
            if (source.editors() == null || source.editors().isEmpty()) {
                if (source.institution() == null || source.institution().isBlank()) {
                    return "Anonymous_" + source.year();
                }
                return String.format("%s_%d", source.institution().split(" ")[0], source.year());
            }
            final String author = Main.formatAuthor(source.editors().getFirst());
            final String lastName = author.substring(0, author.indexOf(","));
            if (source.editors().size() > 1) {
                return String.format("%s_et_al_%d", lastName, source.year());
            }
            return String.format("%s_%d", lastName, source.year());
        }
        final String author = Main.formatAuthor(source.authors().getFirst());
        final String lastName = author.substring(0, author.indexOf(","));
        if (source.authors().size() > 1) {
            return String.format("%s_et_al_%d", lastName, source.year());
        }
        return String.format("%s_%d", lastName, source.year());
    }

    private static void writeSource(final Source source, final BufferedWriter writer) throws IOException {
        switch (source.type()) {
        case ARTICLE:
            writer.write("@article{");
            break;
        case BOOK:
            writer.write("@book{");
            break;
        case HINT:
            return;
        case PROCEEDINGS:
            writer.write("@inproceedings{");
            break;
        default:
            writer.write("@misc{");
        }
        writer.write(
            Main.toSourceId(source)
            .replaceAll("ä", "ae")
            .replaceAll("ö", "oe")
            .replaceAll("ü", "ue")
            .replaceAll("Ä", "Ae")
            .replaceAll("Ö", "Oe")
            .replaceAll("Ü", "Ue")
            .replaceAll("ß", "ss")
        );
        writer.write(",\n  ");
        writer.write(
            Stream.of(
                Main.toSourceEntry("author", Main.toAuthorEntry(source.authors())),
                Main.toSourceEntry("editor", Main.toAuthorEntry(source.editors())),
                Main.toSourceEntry("author", source.institution()),
                Main.toSourceEntry("title", source.title()),
                Main.toSourceEntry("subtitle", source.subtitle()),
                Main.toSourceEntry("journal", source.journal()),
                Main.toSourceEntry("publisher", source.publisher()),
                Main.toSourceEntry("location", source.location()),
                Main.toSourceEntry(
                    "edition",
                    source.edition() != null && source.edition() > 0 ? String.valueOf(source.edition()) : ""
                ),
                Main.toSourceEntry("volume", source.volume()),
                Main.toSourceEntry("number", source.number()),
                Main.toSourceEntry("pages", Main.toPageEntry(source.frompage(), source.topage())),
                Main.toSourceEntry("isbn", source.isbn()),
                Main.toSourceEntry("doi", source.doi()),
                Main.toSourceEntry("url", source.url()),
                Main.toSourceEntry("year", String.valueOf(source.year()))
            ).filter(entry -> !entry.isBlank()).collect(Collectors.joining(",\n  "))
        );
        writer.write("\n}\n\n");
    }

}
