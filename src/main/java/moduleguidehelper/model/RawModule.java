package moduleguidehelper.model;

import java.util.*;

public record RawModule(
    String title,
    String responsible,
    List<String> teachers,
    Language teachinglanguage,
    Language descriptionlanguage,
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
    List<Source> optionalliterature,
    String comment
) {}
