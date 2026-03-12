package moduleguidehelper.model.equivalence;

import java.util.*;

public record ForeignModule(
    String id,
    String title,
    int totalHours,
    List<String> competencies,
    String responsible,
    List<Source> sources
) implements MatchableModule {

}
