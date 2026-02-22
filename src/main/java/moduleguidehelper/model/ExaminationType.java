package moduleguidehelper.model;

import java.util.*;
import java.util.regex.*;

import moduleguidehelper.internationalization.*;

public enum ExaminationType {

    EXAM('K', 2),
    ORAL('M', 3),
    PAPER('S', 5),
    PORTFOLIO('X', 7),
    PRACTICAL('P', 6),
    PRESENTATION('R', 4),
    THESIS('A', 1);

    private static final Comparator<ExaminationType> COMPARATOR = new Comparator<ExaminationType>() {

        @Override
        public int compare(final ExaminationType t1, final ExaminationType t2) {
            return Integer.compare(t1.position, t2.position);
        }

    };

    private static final Pattern EXAMINATION_CODE = Pattern.compile("[AKMRSPX\\*]+");

    public static ExaminationTypes parse(final String examinationTypes) {
        if (!ExaminationType.EXAMINATION_CODE.matcher(examinationTypes).matches()) {
            return null;
        }
        final Set<ExaminationType> types = new TreeSet<ExaminationType>(ExaminationType.COMPARATOR);
        final Set<ExaminationType> preferred = new HashSet<ExaminationType>();
        boolean emphasize = false;
        for (final char c : examinationTypes.toCharArray()) {
            final ExaminationType type = ExaminationType.forCode(c);
            if (emphasize) {
                if (type != null) {
                    preferred.add(type);
                }
                emphasize = false;
            }
            if (type == null) {
                emphasize = true;
            } else {
                types.add(type);
            }
        }
        return new ExaminationTypes(types, preferred);
    }

    private static ExaminationType forCode(final char code) {
        for (final ExaminationType type : ExaminationType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    private final char code;

    private final int position;

    private ExaminationType(final char code, final int position) {
        this.code = code;
        this.position = position;
    }

    public String toString(final Internationalization internationalization) {
        return String.valueOf(this.code);
    }

}
