package modulebookhelper;

import java.util.*;

public record ModuleBook(
    String subject,
    String degree,
    String timemodel,
    String year,
    List<MetaModule> modules
) {

}
