package moduleguidehelper.model;

import java.util.*;

public record MetaModuleGuide(
    String subject,
    String degree,
    TimeModel timemodel,
    String year,
    Language generallanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksspecialization,
    Signature signature,
    List<MetaModule> modules
) {

}
