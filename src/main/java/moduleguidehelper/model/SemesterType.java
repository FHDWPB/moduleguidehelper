package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum SemesterType {

    SEMESTER(InternationalizationKey.SEMESTER),
    TRIMESTER(InternationalizationKey.TRIMESTER);

    public final InternationalizationKey internationalizationKey;

    private SemesterType(final InternationalizationKey internationalizationKey) {
        this.internationalizationKey = internationalizationKey;
    }

}
