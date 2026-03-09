package moduleguidehelper.model;

import java.util.*;

public record ForeignModule(
    String id,
    String name,
    int hours,
    List<String> competencies,
    String responsible,
    List<SourceLink> sources
) {

}
