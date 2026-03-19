package moduleguidehelper.model.equivalence;

import java.util.*;

public record ModuleCheck(String module, String checked, List<Source> sources, boolean decision, int requirements) {

}
