package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum CurriculumMode {

    DUAL(InternationalizationKey.DUAL),
    EXTRA_OCCUPATIONAL(InternationalizationKey.EXTRA_OCCUPATIONAL),
    FULLTIME(InternationalizationKey.FULLTIME);

    public final InternationalizationKey internationalizationKey;

    private CurriculumMode(final InternationalizationKey internationalizationKey) {
        this.internationalizationKey = internationalizationKey;
    }

}
