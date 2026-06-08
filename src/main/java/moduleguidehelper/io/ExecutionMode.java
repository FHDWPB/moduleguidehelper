package moduleguidehelper.io;

import java.util.*;
import java.util.stream.*;

public enum ExecutionMode {

    EQUIVALENCE_CHECK("Compile an equivalence check.", Set.of(Flag.EQUIVALENCE_CHECK, Flag.MODULES)),

    MODULE_GUIDE("Compile a module guide.", Set.of(Flag.GUIDE, Flag.MODULES, Flag.OUTPUT)),

    PRETTY("Pretty print JSON and BIB files.", Set.of(Flag.ROOT)),

    QUARTERLY_OVERVIEW("Compile a quarterly overview of modules.", Set.of(Flag.ROOT, Flag.OUTPUT)),

    SINGLE_MODULES("Compile all modules in isolation.", Set.of(Flag.ROOT));

    public static String descriptions() {
        return Arrays.stream(ExecutionMode.values()).map(mode -> mode.description).collect(Collectors.joining("\n\n"));
    }

    public final String description;

    public final Set<Flag> parameters;

    private ExecutionMode(final String description, final Set<Flag> parameters) {
        this.description = description;
        this.parameters = parameters;
    }

}
