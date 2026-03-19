package moduleguidehelper.model.equivalence;

import java.util.*;

import moduleguidehelper.model.*;

public record OwnModule(ModuleCheck meta, RawModule data) implements MatchableModule {

    @Override
    public int totalHours() {
        return this.data().homehours() + this.data().contacthours();
    }

    @Override
    public List<String> competencies() {
        return this.data().competencies();
    }

    @Override
    public String responsible() {
        return this.data().responsible();
    }

    @Override
    public String title() {
        return this.data().title();
    }

    @Override
    public String id() {
        return this.meta().module();
    }

    @Override
    public List<Source> sources() {
        return this.meta().sources();
    }

}
