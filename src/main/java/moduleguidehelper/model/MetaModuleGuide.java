package moduleguidehelper.model;

import java.util.*;

public record MetaModuleGuide(
    String subject,
    String degree,
    Mode mode,
    String year,
    Language generallanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksspecialization,
    Signature signature,
    List<String> specializationorder,
    List<MetaModule> modules
) {

}
