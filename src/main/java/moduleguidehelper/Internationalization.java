package moduleguidehelper;

public interface Internationalization {

    String electiveHeader(int maxNumber);

    String enumerate(int number);

    String internationalize(InternationalizationKey key);

    String introduction(String degreeType, String subject, String year);

}
