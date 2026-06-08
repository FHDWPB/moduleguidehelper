package moduleguidehelper.model;

import java.util.*;

public record MetaModuleGuide(
    String subject,
    String degree,
    CurriculumMode mode,
    SemesterType semestertype,
    String year,
    int startquarter,
    Integer workphaseswitch,
    Language generallanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksspecialization,
    Signature signature,
    List<String> specializationorder,
    List<MetaModule> modules
) {

}
