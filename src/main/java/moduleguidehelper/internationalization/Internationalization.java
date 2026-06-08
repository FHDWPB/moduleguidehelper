package moduleguidehelper.internationalization;

import java.time.*;

import moduleguidehelper.model.*;

public interface Internationalization {

    String electiveHeader(int maxNumber);

    String enumerate(int number);

    String internationalize(InternationalizationKey key);

    String introduction(String degreeType, String subject, String year);

    String month(YearMonth date);

    String monthYear(YearMonth date);

    String study(CurriculumMode mode);

}
