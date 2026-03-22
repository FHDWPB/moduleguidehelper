package moduleguidehelper.model.equivalence;

import java.io.*;
import java.util.*;

import moduleguidehelper.*;

public record EquivalenceCheckRaw(
    String ourqualification,
    String theirqualification,
    String date,
    String comments,
    List<ModuleCheck> ourmodules,
    String theirmodules,
    String matches,
    List<String> requirements
) {

    public EquivalenceCheck read(final File directory) throws IOException {
        List<ForeignModule> theirModules;
        final File modulesFile = directory.toPath().resolve(this.theirmodules()).toFile();
        try (Reader reader = new FileReader(modulesFile)) {
            theirModules = Main.GSON.fromJson(reader, ForeignModuleList.class);
        }
        Main.prettyPrint(modulesFile, theirModules);
        List<Match> matches;
        final File matchesFile = directory.toPath().resolve(this.matches()).toFile();
        try (Reader reader = new FileReader(matchesFile)) {
            matches = Main.GSON.fromJson(reader, MatchList.class);
        }
        Main.prettyPrint(matchesFile, matches);
        return new EquivalenceCheck(
            this.ourqualification(),
            this.theirqualification(),
            this.date(),
            this.comments(),
            this.ourmodules(),
            theirModules,
            matches,
            this.requirements()
        );
    }

}
