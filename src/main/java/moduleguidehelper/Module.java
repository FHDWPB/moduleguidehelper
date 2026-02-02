package moduleguidehelper;

import java.util.*;

public record Module(
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
    List<String> usability,
    List<String> competencies,
    List<String> teachingmethods,
    List<String> special,
    List<Chapter> content,
    List<Source> requiredliterature,
    List<Source> optionalliterature
) {}
