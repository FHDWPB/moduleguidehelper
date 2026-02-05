package moduleguidehelper;

import java.util.*;

public record ModuleGuide(
    String subject,
    String degree,
    String timemodel,
    String year,
    List<Integer> pagebreaks,
    Signature signature,
    List<MetaModule> modules
) {

}
