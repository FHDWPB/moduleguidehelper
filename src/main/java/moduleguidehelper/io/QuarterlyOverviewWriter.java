package moduleguidehelper.io;

import java.io.*;
import java.time.*;
import java.util.*;

import moduleguidehelper.*;
import moduleguidehelper.model.*;

public class QuarterlyOverviewWriter {

    public static void compileQuarterlyOverview(
        final File root,
        final File outputFile
    ) throws IOException {
        final File modules = root.toPath().resolve("modules").toFile();
        final Set<ModuleEntry> q1 = new TreeSet<ModuleEntry>();
        final Set<ModuleEntry> q2 = new TreeSet<ModuleEntry>();
        final Set<ModuleEntry> q3 = new TreeSet<ModuleEntry>();
        final Set<ModuleEntry> q4 = new TreeSet<ModuleEntry>();
        for (final File json : root.listFiles()) {
            if (!json.getName().endsWith(".json")) {
                continue;
            }
            final ModuleGuide guide = Main.parseModuleGuide(json, modules);
            if (guide.mode() != CurriculumMode.DUAL) {
                continue;
            }
            final ModuleOverview overview = ModuleOverviewBuilder.create(guide);
            int semester = 1;
            final YearMonth start = guide.start();
            for (final List<ModuleStats> statsList : overview.semesters()) {
                final TheoryPhase phase = TheoryPhase.create(guide.mode(), guide.workPhaseSwitch(), semester, start);
                switch ((phase.start().getMonthValue() - 1) / 3) {
                case 0:
                    q1.addAll(statsList.stream().map(QuarterlyOverviewWriter::toEntry).toList());
                    break;
                case 1:
                    q2.addAll(statsList.stream().map(QuarterlyOverviewWriter::toEntry).toList());
                    break;
                case 2:
                    q3.addAll(statsList.stream().map(QuarterlyOverviewWriter::toEntry).toList());
                    break;
                case 3:
                    q4.addAll(statsList.stream().map(QuarterlyOverviewWriter::toEntry).toList());
                    break;
                default:
                    throw new IllegalStateException("Found month greater 12!");
                }
                semester++;
            }
            for (final Map.Entry<Specialization, List<ModuleStats>> entry : overview.specializations().entrySet()) {
                for (final ModuleStats stats : entry.getValue()) {
                    final TheoryPhase phase =
                        TheoryPhase.create(guide.mode(), guide.workPhaseSwitch(), stats.semester(), start);
                    switch ((phase.start().getMonthValue() - 1) / 3) {
                    case 0:
                        q1.add(QuarterlyOverviewWriter.toEntry(stats));
                        break;
                    case 1:
                        q2.add(QuarterlyOverviewWriter.toEntry(stats));
                        break;
                    case 2:
                        q3.add(QuarterlyOverviewWriter.toEntry(stats));
                        break;
                    case 3:
                        q4.add(QuarterlyOverviewWriter.toEntry(stats));
                        break;
                    default:
                        throw new IllegalStateException("Found month greater 12!");
                    }
                }
            }
        }
        try (Writer writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("\\documentclass{article}\n\n");
            writer.write("\\pdfinfoomitdate 1\n");
            writer.write("\\pdftrailerid{}\n");
            writer.write("\\pdfsuppressptexinfo=-1\n\n");
            writer.write("\\usepackage[ngerman,english]{babel}\n");
            writer.write("\\usepackage[T1]{fontenc}\n");
            writer.write("\\usepackage[table]{xcolor}\n");
            writer.write("\\usepackage{graphicx}\n");
            writer.write("\\usepackage[a4paper,margin=2cm]{geometry}\n");
            writer.write("\\usepackage{uarial}\n");
            writer.write("\\usepackage{longtable}\n");
            writer.write("\\usepackage{array}\n");
            writer.write("\\usepackage{colortbl}\n");
            writer.write("\\usepackage{tabularx}\n");
            writer.write("\\usepackage{enumitem}\n");
            writer.write("\\usepackage{tikz}\n");
            writer.write("\\usetikzlibrary{calc,positioning}\n\n");
            writer.write("\\renewcommand{\\familydefault}{\\sfdefault}\n\n");
            writer.write("\\begin{document}\n\n");
            writer.write("\\section*{Quartalsübersicht Module}\n\n");
            QuarterlyOverviewWriter.writeQuarter("Q1", q1, writer);
            QuarterlyOverviewWriter.writeQuarter("Q2", q2, writer);
            QuarterlyOverviewWriter.writeQuarter("Q3", q3, writer);
            QuarterlyOverviewWriter.writeQuarter("Q4", q4, writer);
            writer.write("\\end{document}\n");
        }
    }

    private static ModuleEntry toEntry(final ModuleStats stats) {
        return new ModuleEntry(stats.id(), stats.title());
    }

    private static void writeQuarter(
        final String quarter,
        final Set<ModuleEntry> entries,
        final Writer writer
    ) throws IOException {
        writer.write("\\subsection*{");
        writer.write(quarter);
        writer.write("}\n\n");
        writer.write("\\begin{itemize}\n");
        for (final ModuleEntry entry : entries) {
            if (entry.title().endsWith("aus Spezialisierung") || entry.title().startsWith("Wahlpflichtmodul")) {
                continue;
            }
            writer.write("\\item ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(entry.title()));
            writer.write(" (");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(entry.id()));
            writer.write(")\n");
        }
        writer.write("\\end{itemize}\n");
    }

}
