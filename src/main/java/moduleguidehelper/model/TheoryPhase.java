package moduleguidehelper.model;

import java.time.*;

public record TheoryPhase(YearMonth start, YearMonth end) implements Comparable<TheoryPhase> {

    @Override
    public int compareTo(final TheoryPhase o) {
        final int first = this.start().compareTo(o.start());
        if (first == 0) {
            return this.end().compareTo(o.end());
        }
        return first;
    }

    public boolean sameYear() {
        return this.start().getYear() == this.end().getYear();
    }

    public static TheoryPhase create(
        final CurriculumMode mode,
        final Integer workPhaseSwitch,
        final int semester,
        final YearMonth start
    ) {
        switch (mode) {
        case DUAL:
            if (workPhaseSwitch == null || semester < workPhaseSwitch) {
                return new TheoryPhase(start.plusMonths(semester * 6 - 6), start.plusMonths(semester * 6 - 4));
            } else if (semester >= 5) {
                return new TheoryPhase(start.plusMonths(5 * 6 - 3), start.plusMonths(5 * 6));
            } else {
                return new TheoryPhase(start.plusMonths(semester * 6 - 3), start.plusMonths(semester * 6 - 1));
            }
        default:
            return new TheoryPhase(start.plusMonths(semester * 6 - 6), start.plusMonths(semester * 6 - 1));
        }
    }

}
