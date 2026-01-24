package modulebookhelper;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ModuleBookLaTeXWriter extends ModuleBookWriter {

    private static String chapterToItem(final Chapter chapter) {
        if (chapter.sections() == null || chapter.sections().isEmpty()) {
            return ModuleBookLaTeXWriter.escapeForLaTeX(chapter.chapter());
        }
        final StringWriter stringWriter = new StringWriter();
        stringWriter.write(ModuleBookLaTeXWriter.escapeForLaTeX(chapter.chapter()));
        try (BufferedWriter buffer = new BufferedWriter(stringWriter)) {
            Main.newLine(buffer);
            ModuleBookLaTeXWriter.writeItemize(
                chapter.sections().stream().map(ModuleBookLaTeXWriter::escapeForLaTeX).toList(),
                "",
                buffer
            );
            buffer.flush();
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeForLaTeX(final String text) {
        return text.replaceAll("\\\\", "\\\\textbackslash")
            .replaceAll("([&\\$%\\{\\}_#])", "\\\\$1")
            .replaceAll("~", "\\\\textasciitilde{}")
            .replaceAll("\\^", "\\\\textasciicircum{}")
            .replaceAll("\\\\textbackslash", "\\\\textbackslash{}")
            .replaceAll("([^\\\\])\"", "$1''")
            .replaceAll("^\"", "''");
    }

    private static String formatAuthor(final String author) {
        final String familyName;
        final List<Character> initials = new LinkedList<Character>();
        if (author.contains("|")) {
            final int index = author.indexOf('|');
            familyName = author.substring(index + 1);
            for (final String namePart : author.substring(0, index).split(" ")) {
                if (!namePart.isBlank()) {
                    initials.add(namePart.charAt(0));
                }
            }
        } else {
            final String[] nameParts = author.split(" ");
            familyName = nameParts[nameParts.length - 1];
            for (int i = 0; i < nameParts.length - 1; i++) {
                initials.add(nameParts[i].charAt(0));
            }
        }
        final StringBuilder result = new StringBuilder();
        result.append(ModuleBookLaTeXWriter.escapeForLaTeX(familyName.toUpperCase()));
        result.append(",");
        for (final Character c : initials) {
            result.append(" ");
            result.append(Character.toUpperCase(c));
            result.append(".");
        }
        return result.toString();
    }

    private static String formatExamination(final String examination) {
        if (examination.contains("*")) {
            final int index = examination.indexOf('*');
            final StringBuilder result = new StringBuilder();
            result.append(examination.substring(0, index));
            result.append("\\textbf{");
            result.append(examination.substring(index + 1, index + 2));
            result.append("}");
            result.append(examination.substring(index + 2));
            return result.toString();
        }
        return examination;
    }

    private static List<String> lookupModules(final List<String> ids, final ModuleMap modules) {
        return ids
            .stream()
            .map(id -> modules.containsKey(id) ? modules.get(id).title() : id)
            .map(ModuleBookLaTeXWriter::escapeForLaTeX)
            .toList();
    }

    private static void writeAuthors(final List<String> authors, final BufferedWriter writer) throws IOException {
        writer.write(authors.stream().map(ModuleBookLaTeXWriter::formatAuthor).collect(Collectors.joining(", ")));
    }

    private static void writeItemize(
        final List<String> items,
        final String noItems,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\begin{itemize}[itemsep=0pt,topsep=0pt]");
        Main.newLine(writer);
        if (items.isEmpty()) {
            writer.write("\\item ");
            writer.write(noItems);
            Main.newLine(writer);
        } else {
            for (final String item : items) {
                writer.write("\\item ");
                writer.write(item);
                Main.newLine(writer);
            }
        }
        writer.write("\\end{itemize}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeModule(
        final MetaModule meta,
        final ModuleMap modules,
        final int weightSum,
        final BufferedWriter writer
    ) throws IOException {
        final Module module = modules.get(meta.module());
        if (module == null) {
            System.out.println(meta.module());
            return;
        }
        final String[][] table = new String[13][2];
        table[0][0] = "Kürzel";
        table[0][1] = meta.module();
        table[1][0] = "Modulverantwortliche";
        table[1][1] = ModuleBookLaTeXWriter.escapeForLaTeX(module.responsible());
        table[2][0] = "Dozenten";
        table[2][1] =
            ModuleBookLaTeXWriter.escapeForLaTeX(module.teachers().stream().collect(Collectors.joining(", ")));
        table[3][0] = "Lehrsprache";
        table[3][1] = ModuleBookLaTeXWriter.escapeForLaTeX(module.language());
        table[4][0] = "Semester";
        table[4][1] = String.valueOf(meta.semester());
        table[5][0] = "ECTS-Punkte";
        table[5][1] = String.valueOf(module.ects());
        table[6][0] = "Kontaktstunden";
        table[6][1] = String.valueOf(module.contacthours());
        table[7][0] = "Selbststudium";
        table[7][1] = String.valueOf(module.homehours());
        table[8][0] = "Dauer";
        table[8][1] = meta.duration() + " Semester";
        table[9][0] = "Art";
        table[9][1] = ModuleBookLaTeXWriter.escapeForLaTeX(meta.type());
        table[10][0] = "Häufigkeit";
        table[10][1] = ModuleBookLaTeXWriter.escapeForLaTeX(meta.frequency());
        table[11][0] = "Gewichtung";
        table[11][1] = String.format("%d/%d", meta.weight(), weightSum);
        table[12][0] = "Prüfungsleistung";
        table[12][1] = ModuleBookLaTeXWriter.formatExamination(module.examination());
        writer.write("\\section{");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(module.title()));
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Allgemeine Angaben}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{tabularx}{\\textwidth}{!{\\color{mtabgray}\\vrule}l!{\\color{mtabgray}\\vrule}X!{\\color{mtabgray}\\vrule}}");
        Main.newLine(writer);
        writer.write("\\arrayrulecolor{mtabgray}\\hline");
        Main.newLine(writer);
        for (int i = 0; i < table.length; i++) {
            if (i % 2 == 0) {
                writer.write("\\rowcolor{mtabback}");
                Main.newLine(writer);
            }
            writer.write("\\textbf{");
            writer.write(table[i][0]);
            writer.write("} & ");
            writer.write(table[i][1]);
            writer.write("\\\\\\arrayrulecolor{mtabgray}\\hline");
            Main.newLine(writer);
        }
        writer.write("\\end{tabularx}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.keywords() != null && !module.keywords().isEmpty()) {
            writer.write("\\subsection*{Stichwörter}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleBookLaTeXWriter.writeItemize(
                module.keywords().stream().map(ModuleBookLaTeXWriter::escapeForLaTeX).toList(),
                "Keine",
                writer
            );
        }
        writer.write("\\subsection*{Zugangsvoraussetzungen}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleBookLaTeXWriter.writeItemize(
            ModuleBookLaTeXWriter.lookupModules(module.preconditions(), modules),
            "Keine",
            writer
        );
        writer.write("\\subsection*{Verwendbarkeit}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleBookLaTeXWriter.writeItemize(
            ModuleBookLaTeXWriter.lookupModules(module.usability(), modules),
            "Keine",
            writer
        );
        writer.write("\\subsection*{Qualifikations- und Kompetenzziele}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleBookLaTeXWriter.writeText(module.competencies(), writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Lehr- und Lernmethoden}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.teachingmethods().isEmpty()) {
            writer.write("Präsenzveranstaltungen, Eigenstudium, individuelles und kooperatives Lernen, ");
            writer.write("problemorientiertes und integratives Lernen, forschendes Lernen, synchrones und ");
            writer.write("asynchrones Lernen, Übungen, Fallstudien, Expertenvorträge.");
            Main.newLine(writer);
        } else {
            ModuleBookLaTeXWriter.writeText(module.teachingmethods(), writer);
        }
        Main.newLine(writer);
        if (module.special() != null && !module.special().isEmpty()) {
            writer.write("\\subsection*{Besonderheiten}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleBookLaTeXWriter.writeText(module.special(), writer);
            Main.newLine(writer);
        }
        writer.write("\\subsection*{Inhalte}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.content().getFirst().chapter().startsWith("!")) {
            writer.write(module.content().getFirst().chapter().substring(1));
            Main.newLine(writer);
            Main.newLine(writer);
        } else {
            ModuleBookLaTeXWriter.writeItemize(
                module.content().stream().map(ModuleBookLaTeXWriter::chapterToItem).toList(),
                "keine",
                writer
            );
        }
        if (module.literature() != null && ! module.literature().isEmpty()) {
            writer.write("\\subsection*{Grundlegende Literaturhinweise}");
            Main.newLine(writer);
            Main.newLine(writer);
            for (final Source source : module.literature()) {
                ModuleBookLaTeXWriter.writeSource(source, writer);
            }
            Main.newLine(writer);
        }
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeSource(final Source source, final BufferedWriter writer) throws IOException {
        ModuleBookLaTeXWriter.writeAuthors(source.authors(), writer);
        if (source.year() != null) {
            writer.write(", ");
            writer.write(String.valueOf(source.year()));
        }
        writer.write(". \\textit{");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(source.title()));
        writer.write("}, ");
        if (source.location() != null && !source.location().isBlank()) {
            writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(source.location()));
            writer.write(": ");
        }
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(source.publisher()));
        writer.write(".\\\\[1.5ex]");
        Main.newLine(writer);
    }

    private static void writeText(final List<String> sentences, final BufferedWriter writer) throws IOException {
        writer.write(
            sentences
            .stream()
            .map(ModuleBookLaTeXWriter::escapeForLaTeX)
            .collect(Collectors.joining(Main.lineSeparator))
        );
        Main.newLine(writer);
    }

    public ModuleBookLaTeXWriter(final ModuleBook book, final ModuleMap modules) {
        super(book, modules);
    }

    @Override
    protected void writeDocumentEnd(final BufferedWriter writer) throws IOException {
        Main.newLine(writer);
        writer.write("\\end{document}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeDocumentStart(final BufferedWriter writer) throws IOException {
        writer.write("\\documentclass[12pt]{article}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\usepackage[ngerman]{babel}");
        Main.newLine(writer);
        writer.write("\\usepackage[T1]{fontenc}");
        Main.newLine(writer);
        writer.write("\\usepackage[table]{xcolor}");
        Main.newLine(writer);
        writer.write("\\usepackage{graphicx}");
        Main.newLine(writer);
        writer.write("\\usepackage[a4paper,margin=2cm]{geometry}");
        Main.newLine(writer);
        writer.write("\\usepackage{uarial}");
        Main.newLine(writer);
        writer.write("\\usepackage{longtable}");
        Main.newLine(writer);
        writer.write("\\usepackage{lastpage}");
        Main.newLine(writer);
        writer.write("\\usepackage{fancyhdr}");
        Main.newLine(writer);
        writer.write("\\usepackage{array}");
        Main.newLine(writer);
        writer.write("\\usepackage{colortbl}");
        Main.newLine(writer);
        writer.write("\\usepackage{tabularx}");
        Main.newLine(writer);
        writer.write("\\usepackage{titlesec}");
        Main.newLine(writer);
        writer.write("\\usepackage{titletoc}");
        Main.newLine(writer);
        writer.write("\\usepackage{enumitem}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\titleformat{\\section}{\\normalfont\\Large\\bfseries}{}{0em}{}[{\\titlerule[1pt]}]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(
            "\\titlecontents{section}[0em]{\\vskip 0.5ex}{\\contentsmargin{0pt}}{}{\\titlerule*[3pt]{.}\\contentspage}"
        );
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\renewcommand{\\familydefault}{\\sfdefault}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\newcolumntype{C}[1]{>{\\centering\\let\\newline\\\\\\arraybackslash\\hspace{0pt}}m{#1}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\setlength{\\parindent}{0pt}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\colorlet{mtabgray}{black!50}");
        Main.newLine(writer);
        writer.write("\\colorlet{mtabback}{black!10}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\fancyhead{}");
        Main.newLine(writer);
        writer.write("\\renewcommand{\\headrulewidth}{0pt}");
        Main.newLine(writer);
        writer.write("\\cfoot{}");
        Main.newLine(writer);
        writer.write("\\rfoot{Seite \\thepage{} von \\pageref{LastPage}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{document}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeIntro(final ModuleBook book, final BufferedWriter writer) throws IOException {
        writer.write("\\pagestyle{fancy}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Sehr geehrte Studierende,\\\\");
        Main.newLine(writer);
        writer.write("sehr geehrte Kooperationspartner,\\\\");
        Main.newLine(writer);
        writer.write("sehr geehrte Kolleginnen und Kollegen,\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Sie erhalten das Modulhandbuch für den ");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.degree().substring(0, book.degree().indexOf(' '))));
        writer.write("-Studiengang ");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.subject()));
        writer.write(" im Studienjahr ");
        writer.write(book.year());
        writer.write(".\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Dieses Modulhandbuch stellt zum einen für die Studierenden eine Information über ");
        Main.newLine(writer);
        writer.write("die Studieninhalte dar, zum Zweiten dient es den Partnerunternehmen als Hilfe zur inhaltlichen ");
        Main.newLine(writer);
        writer.write("Vorbereitung der Praxisphasen. Daneben ist diese Übersicht ein Leitfaden für die Dozentinnen ");
        Main.newLine(writer);
        writer.write("und Dozenten zur modulübergreifenden Abstimmung der Lehrinhalte.\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Mit freundlichen Grüßen");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\includegraphics{signature.png}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Prof. Dr. Gregor Sandhaus\\\\");
        Main.newLine(writer);
        writer.write("Dekan des Fachbereichs Informatik");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeModules(
        final ModuleBook book,
        final ModuleMap modules,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\tableofcontents");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
        final int weightSum = book.modules().stream().mapToInt(MetaModule::weight).sum();
        for (final MetaModule meta : book.modules()) {
            ModuleBookLaTeXWriter.writeModule(meta, modules, weightSum, writer);
        }
    }

    @Override
    protected void writeOverview(
        final ModuleBook book,
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\textbf{Modulübersicht}\\\\[1.5ex]");
        Main.newLine(writer);
        writer.write("\\textbf{");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.subject()));
        writer.write(" -- ");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.degree()));
        writer.write("}\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\renewcommand{\\arraystretch}{1.5}");
        Main.newLine(writer);
        writer.write("\\begin{longtable}{|l|*{3}{C{1.1cm}|}C{1.6cm}|C{2.7cm}|}");
        Main.newLine(writer);
        writer.write("\\hline");
        Main.newLine(writer);
        writer.write("\\rule{0pt}{9mm}\\begin{minipage}{6.7cm}\\textbf{Modul}\\end{minipage} & ");
        writer.write("\\begin{minipage}{1cm}\\rotatebox{270}{\\begin{minipage}{1.8cm}\\begin{center}");
        writer.write("\\textbf{Semes\\-ter}");
        writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{minipage} & ");
        writer.write("\\begin{minipage}{1cm}\\rotatebox{270}{\\begin{minipage}{1.8cm}\\begin{center}");
        writer.write("\\textbf{Kontakt\\-stunden}");
        writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{minipage} & ");
        writer.write("\\begin{minipage}{1cm}\\rotatebox{270}{\\begin{minipage}{1.8cm}\\begin{center}");
        writer.write("\\textbf{Selbst\\-studium}");
        writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{minipage} & ");
        writer.write("\\begin{minipage}{1.5cm}\\rotatebox{270}{\\begin{minipage}{1.8cm}\\begin{center}");
        writer.write("\\textbf{Credit Points (ECTS)}");
        writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{minipage} & ");
        writer.write("\\begin{minipage}{2.6cm}\\rotatebox{270}{\\begin{minipage}{1.8cm}\\begin{center}");
        writer.write("\\textbf{Art und Umfang der Prüfungsleistung}");
        writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{minipage}\\\\\\hline");
        Main.newLine(writer);
        int semester = 1;
        for (final List<ModuleStats> modules : overview.semesters()) {
            if (semester > 1) {
                writer.write(" &  &  &  &  & \\\\\\hline");
                Main.newLine(writer);
            }
            writer.write("\\textbf{");
            writer.write(String.valueOf(semester));
            writer.write(". Semester} &  &  &  &  & \\\\\\hline");
            Main.newLine(writer);
            for (final ModuleStats stats : modules) {
                writer.write("\\begin{minipage}{6.7cm}\\strut{}");
                writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(stats.title()));
                writer.write("\\strut{}\\end{minipage}");
                writer.write(" & ");
                writer.write(String.valueOf(semester));
                writer.write(" & ");
                writer.write(String.valueOf(stats.contactHours()));
                writer.write(" & ");
                writer.write(String.valueOf(stats.homeHours()));
                writer.write(" & ");
                writer.write(String.valueOf(stats.ects()));
                writer.write(" & ");
                writer.write(ModuleBookLaTeXWriter.formatExamination(stats.examination()));
                writer.write("\\\\\\hline");
                Main.newLine(writer);
            }
            semester++;
        }
        writer.write("\\end{longtable}");
        Main.newLine(writer);
        writer.write("\\renewcommand{\\arraystretch}{1}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeTitlePage(final ModuleBook book, final BufferedWriter writer) throws IOException {
        writer.write("\\pagestyle{empty}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{center}");
        Main.newLine(writer);
        writer.write("\\Huge");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\textbf{Fachhochschule der Wirtschaft}\\\\");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vspace*{2cm}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\includegraphics{fhdwlogo.png}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vspace*{2cm}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\textbf{Modulhandbuch}\\\\");
        Main.newLine(writer);
        writer.write("\\textbf{(");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.timemodel()));
        writer.write(")}\\\\");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vfill");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\textbf{");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.subject()));
        writer.write("}\\\\");
        Main.newLine(writer);
        writer.write("\\textbf{(");
        writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(book.degree()));
        writer.write(")}\\\\");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vfill");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\textbf{Studienjahr ");
        writer.write(book.year());
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\end{center}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

}
