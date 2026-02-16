package moduleguidehelper;

import java.util.*;

public record ModuleGuide(
    String subject,
    String degree,
    TimeModel timeModel,
    String year,
    Language generalLanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksSpecialization,
    Signature signature,
    List<Module> modules
) {

    public String title() {
        return String.format(
            "%s (%s, %s, %s)",
            this.subject(),
            this.degree(),
            this.generalLanguage().getInternationalization().internationalize(this.timeModel().internationalizationKey),
            this.year()
        );
    }

}
