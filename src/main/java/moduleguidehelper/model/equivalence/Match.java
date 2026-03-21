package moduleguidehelper.model.equivalence;

import java.util.*;

public record Match(String ourID, String theirID, int hours, Map<Integer, Integer> competencyMatch) {}
