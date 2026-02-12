package moduleguidehelper;

import java.util.*;

public record RawModule(
    String title,
    String responsible,
    List<String> teachers,
    String language,
    int ects,
    int contacthours,
    int homehours,
    String examination,
    List<String> keywords,
    List<String> preconditions,
    List<String> recommendations,
    List<String> usability,
    String competenciespreface,
    List<String> competencies,
    List<String> teachingmethods,
    String teachingpostface,
    List<Chapter> content,
    List<Source> requiredliterature,
    List<Source> optionalliterature
) {}
