package moduleguidehelper.model;

import java.util.*;

public record ExaminationTypes(Set<ExaminationType> types, Set<ExaminationType> preferred) {

}
