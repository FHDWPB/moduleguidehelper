package moduleguidehelper;

import java.util.*;

public record Module(
    String title,
    String responsible,
    String language,
    int ects,
    int contacthours,
    int homehours,
    String examination,
    List<String> keywords,
    List<String> preconditions,
    List<String> recommendations,
    String competenciespreface,
    List<String> competencies,
    List<String> teachingmethods,
    String teachingpostface,
    List<Chapter> content,
    List<Source> requiredliterature,
    List<Source> optionalliterature
) {}
