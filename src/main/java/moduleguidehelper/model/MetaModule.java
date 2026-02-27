package moduleguidehelper.model;

import moduleguidehelper.*;

public record MetaModule(
    String module,
    int semester,
    Integer sempos,
    String type,
    String frequency,
    int weight,
    int duration,
    String responsible,
    String contacthoursfactor,
    String homehoursfactor,
    String ectsfactor,
    String specialization,
    Integer specializationnumber
) implements Comparable<MetaModule> {

    @Override
    public int compareTo(final MetaModule o) {
        if (this.specializationnumber() != null) {
            if (o.specializationnumber() != null) {
                if (Main.ELECTIVE.equals(this.specialization())) {
                    if (Main.ELECTIVE.equals(o.specialization())) {
                        return this.specializationnumber().compareTo(o.specializationnumber());
                    }
                    return 1;
                }
                if (Main.ELECTIVE.equals(o.specialization())) {
                    return -1;
                }
                final int compare = this.specialization().compareTo(o.specialization());
                if (compare != 0) {
                    return compare;
                }
                return this.specializationnumber().compareTo(o.specializationnumber());
            }
            return 1;
        }
        if (o.specializationnumber() != null) {
            return -1;
        }
        final int compare = this.semester() - o.semester();
        if (compare != 0) {
            return compare;
        }
        if (this.sempos() != null) {
            if (o.sempos() != null) {
                return this.sempos().compareTo(o.sempos());
            }
            return -1;
        }
        if (o.sempos() != null) {
            return 1;
        }
        return 0;
    }

}
