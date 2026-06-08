package moduleguidehelper.model;

import java.time.*;
import java.util.*;

public record ModuleGuide(
    String subject,
    String degree,
    CurriculumMode mode,
    SemesterType semesterType,
    String year,
    int startQuarter,
    Integer workPhaseSwitch,
    Language generalLanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksSpecialization,
    Signature signature,
    List<String> specializationOrder,
    List<Module> modules
) {

    public YearMonth start() {
        return YearMonth.of(Integer.parseInt(this.year().substring(0, 4)), this.startQuarter() * 3 - 2);
    }

    public String title() {
        return String.format(
            "%s (%s, %s, %s)",
            this.subject(),
            this.degree(),
            this.generalLanguage().getInternationalization().internationalize(this.mode().internationalizationKey),
            this.year()
        );
    }

}
