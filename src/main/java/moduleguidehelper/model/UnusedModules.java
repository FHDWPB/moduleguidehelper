package moduleguidehelper.model;

import java.io.*;
import java.util.*;

import moduleguidehelper.*;

public class UnusedModules extends TreeSet<String> {

    private static final long serialVersionUID = 1L;

    public UnusedModules(final File root) throws IOException {
        final File modulesFolder = root.toPath().resolve(Main.MODULES_FOLDER).toFile();
        for (final String name : modulesFolder.list()) {
            this.add(name.substring(0, name.length() - 5).toUpperCase());
        }
        for (final File guideFile : new ModuleGuides(root)) {
            final ModuleGuide guide = Main.parseModuleGuide(guideFile, modulesFolder);
            for (final Module module : guide.modules()) {
                this.remove(module.meta().module());
            }
        }
    }

    public void write(final File output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            for (final String module : this) {
                writer.write(module);
                writer.write("\n");
            }
        }
    }

}
