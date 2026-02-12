package moduleguidehelper;

import java.util.*;

public record MetaModuleGuide(
    String subject,
    String degree,
    String timemodel,
    String year,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksspecialization,
    Signature signature,
    List<MetaModule> modules
) {

}
