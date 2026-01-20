package modulebookhelper;

public record MetaModule(
    String module,
    int semester,
    String type,
    String frequency,
    int weight,
    int duration,
    String contacthoursfactor,
    String homehoursfactor,
    String ectsfactor,
    String specialization,
    Integer specializationNumber
) {

}
