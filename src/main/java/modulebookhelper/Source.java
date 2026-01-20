package modulebookhelper;

import java.util.*;
import java.util.stream.*;

public record Source(
    List<String> authors,
    String title,
    SourceType type,
    Integer year,
    String publisher,
    String location,
    Integer volume,
    Integer number
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
        if (this.volume() != null) {
            result.append(" ");
            switch (this.type()) {
            case BOOK:
                result.append(this.volume());
                result.append(". Auflage.");
                break;
            default:
                result.append("Ausgabe ");
                result.append(this.volume());
                if (this.number() != null) {
                    result.append(", Nummer ");
                    result.append(this.number());
                }
                result.append(".");
            }
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
