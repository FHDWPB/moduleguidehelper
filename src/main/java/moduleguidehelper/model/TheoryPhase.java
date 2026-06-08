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

}
