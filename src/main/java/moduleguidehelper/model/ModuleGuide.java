package moduleguidehelper.model;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public record ModuleGuide(
    String subject,
    String degree,
    CurriculumMode mode,
    String year,
    Language generalLanguage,
    List<Integer> pagebreaks,
    List<Integer> pagebreaksSpecialization,
    Signature signature,
    List<String> specializationOrder,
    List<Module> modules
) {

    private static final String INDENT = "    ";

    private static void appendInfixIntField(
        final String field,
        final int number,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        ModuleGuide.appendIntField(field, number, contentLengths, writer);
        writer.write(", ");
    }

    private static void appendInfixStringField(
        final String field,
        final String content,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        ModuleGuide.appendStringField(field, content, writer);
        writer.write(", ");
        ModuleGuide.appendWhitespace(contentLengths.get(field), content.length(), writer);
    }

    private static void appendIntField(
        final String field,
        final int number,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        final String content = String.valueOf(number);
        writer.write("\"");
        writer.write(field);
        writer.write("\": ");
        ModuleGuide.appendWhitespace(contentLengths.get(field), content.length(), writer);
        writer.write(content);
    }

    private static void appendStringField(
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        ModuleGuide.printStringField(0, field, content, writer);
    }

    private static void appendStringFieldIfExists(
        final String field,
        final MetaModule module,
        final Function<MetaModule, String> getter,
        final Writer writer
    ) throws IOException {
        final String content = getter.apply(module);
        if (content != null && !content.isBlank()) {
            writer.write(", ");
            ModuleGuide.appendStringField(field, content, writer);
        }
    }

    private static void appendWhitespace(final int max, final int current, final Writer writer) throws IOException {
        writer.write(" ".repeat(max - current));
    }

    private static Map<String, Integer> computeMaxContentLengths(final List<MetaModule> metaModules) {
        final Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (final MetaModule module : metaModules) {
            result.merge("module", module.module().length(), Math::max);
            result.merge(
                "specialization",
                module.specialization() == null ? 0 : module.specialization().length(),
                Math::max
            );
            result.merge(
                "specializationnumber",
                String.valueOf(module.specializationnumber() == null ? 0 : module.specializationnumber()).length(),
                Math::max
            );
            result.merge("semester", String.valueOf(module.semester()).length(), Math::max);
            result.merge("sempos", String.valueOf(module.sempos() == null ? 0 : module.sempos()).length(), Math::max);
            result.merge("type", module.type().length(), Math::max);
            result.merge("duration", String.valueOf(module.duration()).length(), Math::max);
            result.merge("frequency", module.frequency().length(), Math::max);
            result.merge("weight", String.valueOf(module.weight()).length(), Math::max);
        }
        return result;
    }

    private static String escapeJSON(final String content) {
        return content.replace("\\", "\\\\");
    }

    private static boolean groupChange(
        final MetaModule module,
        final int semester,
        final String specialization,
        final int specializationNumber
    ) {
        return (
                module.specialization() != null && !specialization.equals(module.specialization())
            ) || (
                "Wahlpflicht".equals(module.specialization())
                && module.specializationnumber().intValue() != specializationNumber
            ) || (
                (module.specialization() == null || module.specialization().isBlank())
                && module.semester() != semester
            );
    }

    private static void printInfixIntArrayIfExists(
        final String field,
        final Collection<Integer> array,
        final Writer writer
    ) throws IOException {
        if (array == null) {
            return;
        }
        writer.write(ModuleGuide.INDENT);
        writer.write("\"");
        writer.write(field);
        writer.write("\": [");
        writer.write(array.stream().map(String::valueOf).collect(Collectors.joining(",")));
        writer.write("],\n");
    }

    private static void printModule(
        final MetaModule module,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException{
        writer.write(ModuleGuide.INDENT);
        writer.write(ModuleGuide.INDENT);
        writer.write("{");
        ModuleGuide.appendInfixStringField("module", module.module(), contentLengths, writer);
        if (module.specialization() != null && !module.specialization().isBlank()) {
            if ("Wahlpflicht".equals(module.specialization())) {
                ModuleGuide.appendStringField("specialization", module.specialization(), writer);
                writer.write(", ");
            } else {
                ModuleGuide.appendInfixStringField("specialization", module.specialization(), contentLengths, writer);
            }
            ModuleGuide.appendInfixIntField(
                "specializationnumber",
                module.specializationnumber(),
                contentLengths,
                writer
            );
        }
        ModuleGuide.appendInfixIntField("semester", module.semester(), contentLengths, writer);
        ModuleGuide.appendInfixIntField("sempos", module.sempos(), contentLengths, writer);
        ModuleGuide.appendInfixStringField("type", module.type(), contentLengths, writer);
        ModuleGuide.appendInfixIntField("duration", module.duration(), contentLengths, writer);
        ModuleGuide.appendInfixStringField("frequency", module.frequency(), contentLengths, writer);
        ModuleGuide.appendIntField("weight", module.weight(), contentLengths, writer);
        ModuleGuide.appendStringFieldIfExists("responsible", module, MetaModule::responsible, writer);
        ModuleGuide.appendStringFieldIfExists("contacthoursfactor", module, MetaModule::contacthoursfactor, writer);
        ModuleGuide.appendStringFieldIfExists("homehoursfactor", module, MetaModule::homehoursfactor, writer);
        ModuleGuide.appendStringFieldIfExists("ectsfactor", module, MetaModule::ectsfactor, writer);
        writer.write("}");
    }

    private static void printInfixStringArrayIfExists(
        final String field,
        final Collection<String> array,
        final Writer writer
    ) throws IOException {
        if (array == null) {
            return;
        }
        writer.write(ModuleGuide.INDENT);
        writer.write("\"");
        writer.write(field);
        writer.write("\": [\n");
        writer.write(
            array
            .stream()
            .filter(text -> text != null && !text.isBlank())
            .map(text ->
                String.format("%s%s\"%s\"", ModuleGuide.INDENT, ModuleGuide.INDENT, ModuleGuide.escapeJSON(text))
            ).collect(Collectors.joining(",\n"))
        );
        writer.write("\n");
        writer.write(ModuleGuide.INDENT);
        writer.write("],\n");
    }

    private static void printStringField(
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        ModuleGuide.printStringField(1, field, content, writer);
    }

    private static void printStringField(
        final int indentationLevel,
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        for (int i = 0; i < indentationLevel; i++) {
            writer.write(ModuleGuide.INDENT);
        }
        writer.write("\"");
        writer.write(field);
        writer.write("\": \"");
        writer.write(ModuleGuide.escapeJSON(content));
        writer.write("\"");
    }

    public void prettyPrint(final Writer writer) throws IOException {
        writer.write("{\n");
        ModuleGuide.printStringField("subject", this.subject(), writer);
        writer.write(",\n");
        ModuleGuide.printStringField("degree", this.degree(), writer);
        writer.write(",\n");
        ModuleGuide.printStringField("mode", this.mode().name(), writer);
        writer.write(",\n");
        ModuleGuide.printStringField("year", this.year(), writer);
        writer.write(",\n");
        ModuleGuide.printStringField("generallanguage", this.generalLanguage().name(), writer);
        writer.write(",\n");
        ModuleGuide.printInfixIntArrayIfExists("pagebreaks", this.pagebreaks(), writer);
        ModuleGuide.printInfixIntArrayIfExists("pagebreaksspecialization", this.pagebreaksSpecialization(), writer);
        ModuleGuide.printStringField("signature", this.signature().name(), writer);
        writer.write(",\n");
        ModuleGuide.printInfixStringArrayIfExists("specializationorder", this.specializationOrder(), writer);
        writer.write(ModuleGuide.INDENT);
        writer.write("\"modules\": [\n");
        final List<MetaModule> metaModules =
            this
            .modules()
            .stream()
            .sorted(Module.createComparator(this.specializationOrder()))
            .map(Module::meta)
            .toList();
        final Map<String, Integer> contentLengths = ModuleGuide.computeMaxContentLengths(metaModules);
        boolean first = true;
        String specialization = "";
        int semester = 1;
        int specializationNumber = 1;
        for (final MetaModule module : metaModules) {
            if (first) {
                first = false;
            } else {
                writer.write(",\n");
            }
            if (ModuleGuide.groupChange(module, semester, specialization, specializationNumber)) {
                writer.write("\n");
                semester = module.semester();
                specialization = module.specialization() == null ? "" : module.specialization();
                specializationNumber = module.specializationnumber() == null ? 1 : module.specializationnumber();
            }
            ModuleGuide.printModule(module, contentLengths, writer);
        }
        writer.write("\n");
        writer.write(ModuleGuide.INDENT);
        writer.write("]\n}\n");
    }

    public String title() {
        return String.format(
            "%s (%s, %s, %s)",
            this.subject(),
            this.degree(),
            this.generalLanguage().getInternationalization().internationalize(this.mode().internationalizationKey),
            this.year()
        );
    }

}
