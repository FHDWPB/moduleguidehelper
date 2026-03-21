package moduleguidehelper.store;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;

import com.google.gson.*;
import com.google.gson.stream.*;

import moduleguidehelper.*;
import moduleguidehelper.io.*;
import moduleguidehelper.model.*;

public class Store {

    public static final Store INSTANCE = new Store();

    private static int buildPDF(
        final int initial,
        final int total,
        final String fileName,
        final String texFile,
        final File directory,
        final Consumer<Integer> progressListener
    ) throws IOException, InterruptedException {
        int current = initial;
        Process pdfProcess = Main.buildAndStartPDFLaTeXProcess(texFile, directory);
        pdfProcess.waitFor(60, TimeUnit.SECONDS);
        current++;
        progressListener.accept(current * 100 / total);
        final Process biberProcess = Main.buildAndStartBiberProcess(fileName, directory);
        biberProcess.waitFor(60, TimeUnit.SECONDS);
        current++;
        progressListener.accept(current * 100 / total);
        for (int i = 0; i < 3; i++) {
            pdfProcess = Main.buildAndStartPDFLaTeXProcess(texFile, directory);
            pdfProcess.waitFor(60, TimeUnit.SECONDS);
            current++;
            progressListener.accept(current * 100 / total);
        }
        return current;
    }

    private final List<FileSelectionObserver> fileObservers;

    private Set<File> guides;

    private Set<File> modules;

    private Store() {
        this.guides = new LinkedHashSet<File>();
        this.modules = new LinkedHashSet<File>();
        this.fileObservers = new LinkedList<FileSelectionObserver>();
    }

    public void generatePDFs(final File directory, final Consumer<Integer> progressListener) throws Exception {
        final String texSuffix = ".tex";
        final File modules = directory.toPath().resolve("modules").toFile();
        final File singlePDFsDirectory = directory.toPath().resolve(Main.SINGLE_PDFS).toFile();
        Main.compileAllModules(directory);
        final Set<File> files = this.getAllSelectedFiles();
        final int total = 5 * files.size();
        int current = 0;
        progressListener.accept(0);
        for (final File guide : this.guides) {
            final String fileName = guide.getName().substring(0, guide.getName().length() - 5);
            final String texFile = fileName + texSuffix;
            Main.compileModuleGuide(guide, modules, directory.toPath().resolve(texFile).toFile());
            current = Store.buildPDF(current, total, fileName, texFile, directory, progressListener);
        }
        for (final File module : this.modules) {
            if ("schema.json".equals(module.getName())) {
                continue;
            }
            final String fileName = module.getName().substring(0, module.getName().length() - 5);
            final String texFile = fileName + texSuffix;
            final RawModule raw;
            try (FileReader moduleReader = new FileReader(module)) {
                raw = Main.GSON.fromJson(moduleReader, RawModule.class);
            } catch (final MalformedJsonException | JsonSyntaxException e) {
                Main.LOGGER.log(Level.SEVERE, module.getAbsolutePath());
                throw new IOException(String.format("%s: %s", module.getAbsolutePath(), e.getMessage()), e);
            }
            final File moduleTeXFile = singlePDFsDirectory.toPath().resolve(texFile).toFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(moduleTeXFile))) {
                ModuleGuideLaTeXWriter.writeModule(fileName.toUpperCase(), raw, 180, modules, writer);
            }
            current = Store.buildPDF(current, total, fileName, texFile, singlePDFsDirectory, progressListener);
        }
        Process process = new ProcessBuilder(
            "git",
            "add",
            "-A"
        ).inheritIO().directory(directory).start();
        process.waitFor(60, TimeUnit.SECONDS);
        process = new ProcessBuilder(
            "git",
            "commit",
            "-m",
            "update"
        ).inheritIO().directory(directory).start();
        process.waitFor(60, TimeUnit.SECONDS);
        process = new ProcessBuilder(
            "git",
            "push"
        ).inheritIO().directory(directory).start();
        process.waitFor(60, TimeUnit.SECONDS);
    }

    public void registerFileObserver(final FileSelectionObserver observer) {
        this.fileObservers.add(observer);
        observer.notify(this.getAllSelectedFiles());
    }

    public void setGuides(final Collection<File> guides) {
        if (!guides.containsAll(this.guides) || !this.guides.containsAll(guides)) {
            this.guides = new LinkedHashSet<File>(guides);
            final Set<File> files = this.getAllSelectedFiles();
            for (final FileSelectionObserver observer : this.fileObservers) {
                observer.notify(files);
            }
        }
    }

    public void setModules(final Collection<File> modules) {
        if (!modules.containsAll(this.modules) || !this.modules.containsAll(modules)) {
            this.modules = new LinkedHashSet<File>(modules);
            final Set<File> files = this.getAllSelectedFiles();
            for (final FileSelectionObserver observer : this.fileObservers) {
                observer.notify(files);
            }
        }
    }

    public void syncgit(
        final File directory,
        final Consumer<Integer> progressListener
    ) throws IOException, InterruptedException {
        progressListener.accept(0);
        final File resetFile = directory.toPath().resolve("reset.log").toFile();
        final File cleanFile = directory.toPath().resolve("clean.log").toFile();
        final File pullFile = directory.toPath().resolve("pull.log").toFile();
        Process process = new ProcessBuilder(
            "git",
            "config",
            "protectNTFS",
            "false"
        ).inheritIO().directory(directory).start();
        process.waitFor(60, TimeUnit.SECONDS);
        progressListener.accept(25);
        process = new ProcessBuilder(
            "git",
            "reset",
            "--hard"
        ).inheritIO().directory(directory).redirectOutput(resetFile).redirectError(resetFile).start();
        process.waitFor(60, TimeUnit.SECONDS);
        progressListener.accept(50);
        process = new ProcessBuilder(
            "git",
            "clean",
            "-f",
            "-d"
        ).inheritIO().directory(directory).redirectOutput(cleanFile).redirectError(cleanFile).start();
        process.waitFor(60, TimeUnit.SECONDS);
        progressListener.accept(75);
        process = new ProcessBuilder(
            "git",
            "pull",
            "--rebase",
            "-X ours"
        ).inheritIO().directory(directory).redirectOutput(pullFile).redirectError(pullFile).start();
        process.waitFor(60, TimeUnit.SECONDS);
        progressListener.accept(100);
        try (BufferedReader reader = new BufferedReader(new FileReader(resetFile))) {
            final String line = reader.readLine();
            if (!line.startsWith("HEAD is now")) {
                throw new IllegalStateException("Fehler beim Reset: " + line);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(cleanFile))) {
            final String line = reader.readLine();
            if (line != null) {
                throw new IllegalStateException("Fehler beim Clean: " + line);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(pullFile))) {
            final String line = reader.readLine();
            if (line.startsWith("error")) {
                throw new IllegalStateException("Fehler beim Pull: " + line);
            }
        }
    }

    private Set<File> getAllSelectedFiles() {
        final Set<File> result = new LinkedHashSet<File>(this.guides);
        result.addAll(this.modules);
        return result;
    }

}
