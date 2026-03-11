package moduleguidehelper.model;

public record MetaModule(
    String module,
    int semester,
    Integer sempos,
    String type,
    String frequency,
    int weight,
    int duration,
    String responsible,
    String contacthoursfactor,
    String homehoursfactor,
    String ectsfactor,
    String specialization,
    Integer specializationnumber
) {

}
