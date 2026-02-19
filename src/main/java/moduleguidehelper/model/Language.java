package moduleguidehelper.model;

import moduleguidehelper.internationalization.*;

public enum Language {

    ENGLISH("Englisch", "English"),
    GERMAN("Deutsch", "German");

    private final String english;

    private final String german;

    private Language(final String german, final String english) {
        this.german = german;
        this.english = english;
    }

    public Internationalization getInternationalization() {
        switch (this) {
        case GERMAN:
            return new German();
        case ENGLISH:
            return new English();
        default:
            return new German();
        }
    }

    public String toString(final Language language) {
        switch (language) {
        case GERMAN:
            return this.german;
        case ENGLISH:
            return this.english;
        default:
            return this.german;
        }
    }

}
