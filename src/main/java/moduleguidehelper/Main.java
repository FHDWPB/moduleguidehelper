package moduleguidehelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import com.google.gson.*;
import com.google.gson.stream.*;

import moduleguidehelper.io.*;
import moduleguidehelper.model.*;
import moduleguidehelper.model.Module;
import moduleguidehelper.model.bibtex.*;
import moduleguidehelper.model.equivalence.*;
import moduleguidehelper.view.*;

public class Main {

    public static final String ELECTIVE = "Wahlpflicht";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static String lineSeparator = "\n";

    public static final Logger LOGGER = Logger.getLogger("moduleguidehelper");

    public static final String SINGLE_PDFS = "singlepdfs";

    private static final String VERSION = "3.3.0";

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
            final File root = new File(args[0]);
            final File singleModulesDirectory = root.toPath().resolve(Main.SINGLE_PDFS).toFile();
            if (!singleModulesDirectory.exists()) {
                singleModulesDirectory.mkdir();
            }
            final File modules = root.toPath().resolve("modules").toFile();
            final File literature = root.toPath().resolve("literature.bib").toFile();
            final BibTeXDatabase db;
            try (final FileReader reader = new FileReader(literature)) {
                db = BibTeXParser.parse(reader);
            } catch (final IOException e) {
                Main.LOGGER.log(Level.SEVERE, e.getMessage());
                throw new IOException(e);
            }
            try (final FileWriter writer = new FileWriter(literature)) {
                final BibTeXFormatter formatter = new BibTeXFormatter();
                formatter.format(db, writer);
            }
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
                final File moduleTeXFile = singleModulesDirectory.toPath().resolve(id + ".tex").toFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(moduleTeXFile))) {
                    ModuleGuideLaTeXWriter.writeModule(id.toUpperCase(), module, 180, modules, writer);
                }
                Main.prettyPrint(json, module);
            }
            return;
        }
        if (args == null || args.length == 2) {
            System.out.println("Expected input: check, ownModules");
            final File checkFile = new File(args[0]);
            EquivalenceCheck check;
            try (Reader reader = new FileReader(checkFile)) {
                check = Main.GSON.fromJson(reader, EquivalenceCheck.class);
            }
            final File outputFile =
                checkFile.toPath()
                .getParent()
                .resolve(checkFile.getName().substring(0, checkFile.getName().length() - 4) + "tex")
                .toFile();
            try (Writer writer = new BufferedWriter(new FileWriter(outputFile))) {
                new Documentation(
                    check.theirqualification(),
                    check.ourqualification(),
                    Files.readAllLines(new File(check.comments()).toPath()),
                    new OwnModuleParser().apply(check, new File(args[1])),
                    check.theirmodules(),
                    check.matches(),
                    check.requirements()
                ).write(writer);
            }
            return;
        }
        Main.LOGGER.setLevel(Level.SEVERE);
        if (args == null || args.length != 3) {
            System.out.println("Call with guide JSON, modules folder, and output file!");
            return;
        }
        final ModuleGuide guide = Main.parseModuleGuide(new File(args[0]), new File(args[1]));
        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]))
        ) {
            new ModuleGuideLaTeXWriter(guide).write(new File(args[1]), writer);
        }
    }

    public static void newLine(final BufferedWriter writer) throws IOException {
        writer.write(Main.lineSeparator);
    }

    public static ModuleGuide parseModuleGuide(final File guide, final File modulesFolder) throws IOException {
        final MetaModuleGuide metaGuide;
        try (FileReader guideReader = new FileReader(guide)) {
            metaGuide = Main.GSON.fromJson(guideReader, MetaModuleGuide.class);
        } catch (RuntimeException | IOException e) {
            throw new IOException(String.format("Exception on module guide %s: %s", guide, e.getMessage()), e);
        }
        final List<Module> modules = new ArrayList<Module>();
        for (final MetaModule meta : metaGuide.modules()) {
            final File moduleJson = modulesFolder.toPath().resolve(meta.module().toLowerCase() + ".json").toFile();
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
            metaGuide.mode(),
            metaGuide.year(),
            metaGuide.generallanguage(),
            metaGuide.pagebreaks(),
            metaGuide.pagebreaksspecialization(),
            metaGuide.signature(),
            metaGuide.specializationorder(),
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

}
