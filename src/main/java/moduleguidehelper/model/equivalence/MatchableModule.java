package moduleguidehelper.model.equivalence;

import java.util.*;

public interface MatchableModule {

    List<String> competencies();

    String id();

    String responsible();

    List<Source> sources();

    String title();

    int totalHours();

}
