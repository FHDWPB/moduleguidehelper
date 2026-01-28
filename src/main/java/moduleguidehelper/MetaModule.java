package moduleguidehelper;

public record MetaModule(
    String module,
    int semester,
    Integer sempos,
    String type,
    String frequency,
    int weight,
    int duration,
    String contacthoursfactor,
    String homehoursfactor,
    String ectsfactor,
    String specialization,
    Integer specializationNumber
) implements Comparable<MetaModule> {

    @Override
    public int compareTo(final MetaModule o) {
        if (this.specializationNumber() != null) {
            if (o.specializationNumber() != null) {
                final int compare = this.specialization().compareTo(o.specialization());
                if (compare != 0) {
                    return compare;
                }
                return this.specializationNumber().compareTo(o.specializationNumber());
            }
            return 1;
        }
        if (o.specializationNumber() != null) {
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
