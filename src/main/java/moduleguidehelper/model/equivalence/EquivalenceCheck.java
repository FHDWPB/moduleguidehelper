package moduleguidehelper.model.equivalence;

import java.util.*;

public record EquivalenceCheck(
    String ourqualification,
    String theirqualification,
    String date,
    String comments,
    List<ModuleCheck> ourmodules,
    List<ForeignModule> theirmodules,
    List<Match> matches,
    List<String> requirements
) {

}
