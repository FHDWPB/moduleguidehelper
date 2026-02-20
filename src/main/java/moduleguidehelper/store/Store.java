package moduleguidehelper.store;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import moduleguidehelper.*;

public class Store {

    public static final Store INSTANCE = new Store();

    private final List<GuideObserver> guideObservers;

    private Set<File> guides;

    private Store() {
        this.guides = new LinkedHashSet<File>();
        this.guideObservers = new LinkedList<GuideObserver>();
    }

    public void generatePDFs(final File directory) throws Exception {
        final String texSuffix = ".tex";
        final String modules = directory.toPath().resolve("modules").toString();
        Main.main(new String[] {modules});
        for (final File guide : this.guides) {
            final String texFile = guide.getName().substring(0, guide.getName().length() - 5) + texSuffix;
            Main.main(
                new String[] {
                    guide.getPath(),
                    modules,
                    directory.toPath().resolve(texFile).toString()
                }
            );
            for (int i = 0; i < 4; i++) {
                final Process pdfProcess = Main.buildAndStartPDFLaTeXProcess(texFile, directory);
                pdfProcess.waitFor(60, TimeUnit.SECONDS);
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

}
