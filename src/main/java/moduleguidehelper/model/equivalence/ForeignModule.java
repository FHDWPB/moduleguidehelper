package moduleguidehelper.model.equivalence;

import java.util.*;

public record ForeignModule(
    String id,
    String title,
    int totalhours,
    List<String> competencies,
    String responsible,
    List<Source> sources
) implements MatchableModule {

}
