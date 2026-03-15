package moduleguidehelper.io;

import java.io.*;
import java.util.*;

import com.google.gson.*;

import moduleguidehelper.model.equivalence.*;

public class ForeignModuleParser implements CheckedFunction<File, List<ForeignModule>, IOException> {

    @Override
    public List<ForeignModule> apply(final File input) throws IOException {
        try (FileReader reader = new FileReader(input)) {
            return new Gson().fromJson(reader, ForeignModuleList.class);
        }
    }

}
