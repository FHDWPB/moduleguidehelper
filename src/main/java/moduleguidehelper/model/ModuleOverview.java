package moduleguidehelper.model;

import java.util.*;

public record ModuleOverview(
    List<List<ModuleStats>> semesters,
    Map<Specialization, List<ModuleStats>> specializations,
    int contactHoursSum,
    int homeHoursSum,
    int ectsSum,
    int weightSum
) {

}
