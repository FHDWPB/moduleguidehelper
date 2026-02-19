package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum TimeModel {

    FULLTIME(InternationalizationKey.FULLTIME),
    PARTTIME(InternationalizationKey.PARTTIME);

    public final InternationalizationKey internationalizationKey;

    private TimeModel(final InternationalizationKey internationalizationKey) {
        this.internationalizationKey = internationalizationKey;
    }

}
