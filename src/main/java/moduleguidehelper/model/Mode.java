package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum Mode {

    DUAL(InternationalizationKey.DUAL),
    EXTRA_OCCUPATIONAL(InternationalizationKey.EXTRA_OCCUPATIONAL),
    FULLTIME(InternationalizationKey.FULLTIME);

    public final InternationalizationKey internationalizationKey;

    private Mode(final InternationalizationKey internationalizationKey) {
        this.internationalizationKey = internationalizationKey;
    }

}
