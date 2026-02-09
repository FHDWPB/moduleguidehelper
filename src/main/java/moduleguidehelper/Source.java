package moduleguidehelper;

import java.util.*;
import java.util.stream.*;

public record Source(
    List<String> authors,
    String title,
    String subtitle,
    SourceType type,
    Integer year,
    String journal,
    String publisher,
    String location,
    Integer edition,
    String volume,
    String number,
    Integer frompage,
    Integer topage,
    String isbn,
    String doi
) {

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.type().name());
        result.append(" ");
        result.append(this.authors().stream().collect(Collectors.joining(", ")));
        result.append(": ");
        result.append(this.title());
        result.append(".");
        switch (this.type()) {
        case BOOK:
            if (this.edition() != null) {
                result.append(" ");
                result.append(this.edition());
                result.append(". Auflage.");
            }
            break;
        case ARTICLE:
            final boolean hasJournal = this.journal() != null && !this.journal().isBlank();
            if (hasJournal) {
                result.append(" ");
                result.append(this.journal());
            }
            final boolean hasVolume = this.volume() != null && !this.volume().isBlank();
            if (hasVolume) {
                if (hasJournal) {
                    result.append(",");
                }
                result.append(" Ausgabe ");
                result.append(this.volume());
            }
            final boolean hasNumber = this.number() != null && !this.number().isBlank();
            if (hasNumber) {
                if (hasJournal || hasVolume) {
                    result.append(",");
                }
                result.append(" Nummer ");
                result.append(this.number());
            }
            if (hasJournal || hasVolume || hasNumber) {
                result.append(".");
            }
            break;
        default:
            // DO NOTHING
        }
        if (this.publisher() != null) {
            result.append(" ");
            result.append(this.publisher());
            if (this.location() != null) {
                result.append(", ");
                result.append(this.location());
            }
            result.append(",");
        }
        result.append(" ");
        result.append(this.year() == null ? "(Ohne Datum)" : this.year().toString());
        result.append(".");
        return result.toString();
    }

}
