package moduleguidehelper.model.equivalence;

import java.util.*;

public record Match(String ownID, String otherID, int hours, Map<Integer, Integer> competencyMatch) {}
