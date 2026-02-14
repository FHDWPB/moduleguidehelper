package moduleguidehelper;

import java.util.*;

public record ModuleGuide(
    String subject,
    String degree,
    String timemodel,
    String year,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksspecialization,
    Signature signature,
    List<Module> modules
) {

    public String title() {
        return String.format("%s (%s, %s, %s)", this.subject(), this.degree(), this.timemodel(), this.year());
    }

}
