package moduleguidehelper.io;

import java.io.*;
import java.util.*;

import com.google.gson.*;

import moduleguidehelper.model.equivalence.*;

public class OwnModuleParser implements CheckedFunction<File, List<OwnModule>, IOException> {

    private final int semester;

    public OwnModuleParser(final int semester) {
        this.semester = semester;
    }

    @Override
    public List<OwnModule> apply(final File input) throws IOException {
        //TODO
        return List.of();
    }

}
