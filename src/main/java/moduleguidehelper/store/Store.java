package moduleguidehelper.store;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import moduleguidehelper.*;

public class Store {

    public static final Store INSTANCE = new Store();

    private final List<GuideObserver> guideObservers;

    private Set<File> guides;

    private Store() {
        this.guides = new LinkedHashSet<File>();
        this.guideObservers = new LinkedList<GuideObserver>();
    }

    public void generatePDFs(final File directory, final Consumer<Integer> progressListener) throws Exception {
        final String texSuffix = ".tex";
        final String modules = directory.toPath().resolve("modules").toString();
        Main.main(new String[] {directory.toPath().toString()});
        final int total = 5 * this.guides.size();
        int current = 0;
        progressListener.accept(0);
        for (final File guide : this.guides) {
            final String fileName = guide.getName().substring(0, guide.getName().length() - 5);
            final String texFile = fileName + texSuffix;
            Main.main(
                new String[] {
                    guide.getPath(),
                    modules,
                    directory.toPath().resolve(texFile).toString()
                }
            );
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

    public void registerGuideObserver(final GuideObserver observer) {
        this.guideObservers.add(observer);
        observer.notify(this.guides);
    }

    public void setGuides(final Collection<File> guides) {
        if (!guides.containsAll(this.guides) || !this.guides.containsAll(guides)) {
            this.guides = new LinkedHashSet<File>(guides);
            for (final GuideObserver observer : this.guideObservers) {
                observer.notify(this.guides);
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

}
