package moduleguidehelper.io;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import moduleguidehelper.model.*;
import moduleguidehelper.model.Module;

public class ModuleGuidePrettyPrinter {

    private static final String INDENT = "    ";

    public static void prettyPrint(final ModuleGuide guide, final Writer writer) throws IOException {
        writer.write("{\n");
        ModuleGuidePrettyPrinter.printStringField("subject", guide.subject(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printStringField("degree", guide.degree(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printStringField("mode", guide.mode().name(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printStringField("semestertype", guide.semesterType().name(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printStringField("year", guide.year(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printIntField("startquarter", guide.startQuarter(), writer);
        writer.write(",\n");
        if (guide.workPhaseSwitch() != null) {
            ModuleGuidePrettyPrinter.printIntField("workphaseswitch", guide.workPhaseSwitch(), writer);
            writer.write(",\n");
        }
        ModuleGuidePrettyPrinter.printStringField("generallanguage", guide.generalLanguage().name(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printInfixIntArrayIfExists("pagebreaks", guide.pagebreaks(), writer);
        ModuleGuidePrettyPrinter.printInfixIntArrayIfExists(
            "pagebreaksspecialization",
            guide.pagebreaksSpecialization(),
            writer
        );
        ModuleGuidePrettyPrinter.printStringField("signature", guide.signature().name(), writer);
        writer.write(",\n");
        ModuleGuidePrettyPrinter.printInfixStringArrayIfExists(
            "specializationorder",
            guide.specializationOrder(),
            writer
        );
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("\"modules\": [\n");
        final List<MetaModule> metaModules =
            guide
            .modules()
            .stream()
            .sorted(Module.createComparator(guide.specializationOrder()))
            .map(Module::meta)
            .toList();
        final Map<String, Integer> contentLengths = ModuleGuidePrettyPrinter.computeMaxContentLengths(metaModules);
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
            if (ModuleGuidePrettyPrinter.groupChange(module, semester, specialization, specializationNumber)) {
                writer.write("\n");
                semester = module.semester();
                specialization = module.specialization() == null ? "" : module.specialization();
                specializationNumber = module.specializationnumber() == null ? 1 : module.specializationnumber();
            }
            ModuleGuidePrettyPrinter.printModule(module, contentLengths, writer);
        }
        writer.write("\n");
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("]\n}\n");
    }

    private static void appendInfixIntField(
        final String field,
        final int number,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        ModuleGuidePrettyPrinter.appendIntField(field, number, contentLengths, writer);
        writer.write(", ");
    }

    private static void appendInfixStringField(
        final String field,
        final String content,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        ModuleGuidePrettyPrinter.appendStringField(field, content, writer);
        writer.write(", ");
        ModuleGuidePrettyPrinter.appendWhitespace(contentLengths.get(field), content.length(), writer);
    }

    private static void appendInfixStringFieldIfExists(
        final String field,
        final MetaModule module,
        final Function<MetaModule, String> getter,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException {
        final String content = getter.apply(module);
        if (content != null && !content.isBlank()) {
            ModuleGuidePrettyPrinter.appendInfixStringField(field, content, contentLengths, writer);
        }
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
        ModuleGuidePrettyPrinter.appendWhitespace(contentLengths.get(field), content.length(), writer);
        writer.write(content);
    }

    private static void appendStringField(
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        ModuleGuidePrettyPrinter.printStringField(0, field, content, writer);
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
            ModuleGuidePrettyPrinter.appendStringField(field, content, writer);
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
            result.merge("semestername", module.semestername() == null ? 0 : module.semestername().length(), Math::max);
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
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("\"");
        writer.write(field);
        writer.write("\": [");
        writer.write(array.stream().map(String::valueOf).collect(Collectors.joining(",")));
        writer.write("],\n");
    }

    private static void printInfixStringArrayIfExists(
        final String field,
        final Collection<String> array,
        final Writer writer
    ) throws IOException {
        if (array == null) {
            return;
        }
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("\"");
        writer.write(field);
        writer.write("\": [\n");
        writer.write(
            array
            .stream()
            .filter(text -> text != null && !text.isBlank())
            .map(text ->
                String.format(
                    "%s%s\"%s\"",
                    ModuleGuidePrettyPrinter.INDENT,
                    ModuleGuidePrettyPrinter.INDENT,
                    ModuleGuidePrettyPrinter.escapeJSON(text)
                )
            ).collect(Collectors.joining(",\n"))
        );
        writer.write("\n");
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("],\n");
    }

    private static void printIntField(final String field, final int content, final Writer writer) throws IOException {
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("\"");
        writer.write(field);
        writer.write("\": ");
        writer.write(String.valueOf(content));
    }

    private static void printModule(
        final MetaModule module,
        final Map<String, Integer> contentLengths,
        final Writer writer
    ) throws IOException{
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write(ModuleGuidePrettyPrinter.INDENT);
        writer.write("{");
        ModuleGuidePrettyPrinter.appendInfixStringField("module", module.module(), contentLengths, writer);
        if (module.specialization() != null && !module.specialization().isBlank()) {
            if ("Wahlpflicht".equals(module.specialization())) {
                ModuleGuidePrettyPrinter.appendStringField("specialization", module.specialization(), writer);
                writer.write(", ");
            } else {
                ModuleGuidePrettyPrinter.appendInfixStringField(
                    "specialization",
                    module.specialization(),
                    contentLengths,
                    writer
                );
            }
            ModuleGuidePrettyPrinter.appendInfixIntField(
                "specializationnumber",
                module.specializationnumber(),
                contentLengths,
                writer
            );
        }
        ModuleGuidePrettyPrinter.appendInfixStringFieldIfExists(
            "semestername",
            module,
            MetaModule::semestername,
            contentLengths,
            writer
        );
        ModuleGuidePrettyPrinter.appendInfixIntField("semester", module.semester(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendInfixIntField("sempos", module.sempos(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendInfixStringField("type", module.type(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendInfixIntField("duration", module.duration(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendInfixStringField("frequency", module.frequency(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendIntField("weight", module.weight(), contentLengths, writer);
        ModuleGuidePrettyPrinter.appendStringFieldIfExists("responsible", module, MetaModule::responsible, writer);
        ModuleGuidePrettyPrinter.appendStringFieldIfExists(
            "contacthoursfactor",
            module,
            MetaModule::contacthoursfactor,
            writer
        );
        ModuleGuidePrettyPrinter.appendStringFieldIfExists(
            "homehoursfactor",
            module,
            MetaModule::homehoursfactor,
            writer
        );
        ModuleGuidePrettyPrinter.appendStringFieldIfExists("ectsfactor", module, MetaModule::ectsfactor, writer);
        writer.write("}");
    }

    private static void printStringField(
        final int indentationLevel,
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        for (int i = 0; i < indentationLevel; i++) {
            writer.write(ModuleGuidePrettyPrinter.INDENT);
        }
        writer.write("\"");
        writer.write(field);
        writer.write("\": \"");
        writer.write(ModuleGuidePrettyPrinter.escapeJSON(content));
        writer.write("\"");
    }

    private static void printStringField(
        final String field,
        final String content,
        final Writer writer
    ) throws IOException {
        ModuleGuidePrettyPrinter.printStringField(1, field, content, writer);
    }

}
