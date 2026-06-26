package moduleguidehelper.io;

import java.util.*;

import clit.*;

public enum Flag implements Parameter {

    EQUIVALENCE_CHECK("e", "equivalence", "Equivalence check file."),

    EXECUTION_MODE("x", "executionmode", "Execution mode."),

    GUIDE("g", "guide", "Module guide file."),

    INPUT("i", "input", "Input file."),

    KEYVALUES("k", "keyvalues", "Mapping from keys to values."),

    MODULES("m", "modules", "Directory containing the modules."),

    OUTPUT("o", "output", "Output file."),

    ROOT("r", "root", "Root directory.");

    private final String description;

    private final String longName;

    private final String shortName;

    private Flag(final String shortName, final String longName, final String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
    }

    @Override
    public Set<Parameter> dependencies() {
        switch (this) {
        case EQUIVALENCE_CHECK:
            return Set.of(Flag.MODULES);
        case GUIDE:
            return Set.of(Flag.MODULES, Flag.OUTPUT);
        default:
            return Collections.emptySet();
        }
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public String longName() {
        return this.longName;
    }

    @Override
    public String shortName() {
        return this.shortName;
    }

}
