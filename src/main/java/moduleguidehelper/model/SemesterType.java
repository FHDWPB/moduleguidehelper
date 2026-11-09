package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum SemesterType {

    SEMESTER(InternationalizationKey.SEMESTER, InternationalizationKey.SEMESTER_HEADLINE),
    TRIMESTER(InternationalizationKey.TRIMESTER, InternationalizationKey.TRIMESTER_HEADLINE);

    public final InternationalizationKey internationalizationKey;

    public final InternationalizationKey internationalizationKeyHeadline;

    private SemesterType(
        final InternationalizationKey internationalizationKey,
        final InternationalizationKey internationalizationKeyHeadline
    ) {
        this.internationalizationKey = internationalizationKey;
        this.internationalizationKeyHeadline = internationalizationKeyHeadline;
    }

}
