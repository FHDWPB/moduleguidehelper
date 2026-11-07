package moduleguidehelper.model;

import java.io.*;
import java.util.*;

import moduleguidehelper.*;

public class ModuleGuides extends LinkedList<File> {

    private static final long serialVersionUID = 1L;

    public ModuleGuides(final File root) {
        for (final File file : root.toPath().resolve(Main.GUIDES_FOLDER).toFile().listFiles()) {
            this.add(file);
        }
    }

}
