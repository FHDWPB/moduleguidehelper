package moduleguidehelper.io;

import java.io.*;
import java.util.*;

import moduleguidehelper.*;
import moduleguidehelper.model.*;
import moduleguidehelper.model.equivalence.*;

public class OwnModuleParser implements CheckedBiFunction<EquivalenceCheck, File, List<OwnModule>, IOException> {

    @Override
    public List<OwnModule> apply(final EquivalenceCheck check, final File moduleFolder) throws IOException {
        final List<OwnModule> result = new ArrayList<OwnModule>();
        for (final ModuleCheck moduleCheck : check.ourmodules()) {
            final File moduleFile =
                moduleFolder.toPath().resolve(moduleCheck.module().toLowerCase() + ".json").toFile();
            final RawModule module;
            try (FileReader reader = new FileReader(moduleFile)) {
                module = Main.GSON.fromJson(reader, RawModule.class);
            }
            result.add(new OwnModule(moduleCheck, module));
        }
        return result;
    }

}
