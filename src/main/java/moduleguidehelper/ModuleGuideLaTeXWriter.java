package moduleguidehelper;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class ModuleGuideLaTeXWriter extends ModuleGuideWriter {

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\$\\$[^\\$]+\\$\\$");

    private static final String OVERVIEW_FIRST_COL_SIZE = "7.2cm";

    public static void writeModule(
        final String id,
        final Module module,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeDocumentStartStatic(writer);
        writer.write("\\pagestyle{fancy}");
        Main.newLine(writer);
        Main.newLine(writer);
        final ModuleMap modules = new ModuleMap();
        modules.put(id, module);
        ModuleGuideLaTeXWriter.writeModule(
            new MetaModule(id, 1, 1, "Pflicht", "Jedes Jahr", 5, 1, "", "", "", "", null),
            modules,
            180,
            List.of(),
            writer
        );
        ModuleGuideLaTeXWriter.writeDocumentEndStatic(writer);
    }

    private static String chapterToItem(final Chapter chapter) {
        if (chapter.sections() == null || chapter.sections().isEmpty()) {
            return ModuleGuideLaTeXWriter.escapeForLaTeX(chapter.chapter());
        }
        final StringWriter stringWriter = new StringWriter();
        stringWriter.write(ModuleGuideLaTeXWriter.escapeForLaTeX(chapter.chapter()));
        try (BufferedWriter buffer = new BufferedWriter(stringWriter)) {
            Main.newLine(buffer);
            ModuleGuideLaTeXWriter.writeItemize(
                chapter.sections().stream().map(ModuleGuideLaTeXWriter::escapeForLaTeX).toList(),
                "",
                "$\\circ$",
                buffer
            );
            buffer.flush();
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeForLaTeX(final String text) {
        final Matcher matcher = ModuleGuideLaTeXWriter.ESCAPE_PATTERN.matcher(text);
        final List<Integer> indices = new LinkedList<Integer>();
        while (matcher.find()) {
            indices.add(matcher.start());
            indices.add(matcher.end());
        }
        indices.add(text.length());
        final StringBuilder result = new StringBuilder();
        boolean escape = true;
        int from = 0;
        for (final Integer index : indices) {
            if (escape) {
                result.append(
                    text
                    .substring(from, index)
                    .replaceAll("\\\\", "\\\\textbackslash")
                    .replaceAll("([&\\$%\\{\\}_#])", "\\\\$1")
                    .replaceAll("~", "\\\\textasciitilde{}")
                    .replaceAll("\\^", "\\\\textasciicircum{}")
                    .replaceAll("\\\\textbackslash", "\\\\textbackslash{}")
                    .replaceAll("([^\\\\])\"", "$1''")
                    .replaceAll("^\"", "''")
                );
            } else {
                result.append(text.substring(from + 2, index - 2));
            }
            from = index;
            escape = !escape;
        }
        return result.toString();
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
        result.append(ModuleGuideLaTeXWriter.escapeForLaTeX(familyName.toUpperCase()));
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

    private static String lookupModule(
        final String id,
        final ModuleMap modules,
        final List<String> linkable
    ) {
        if (modules.containsKey(id)) {
            final String title = ModuleGuideLaTeXWriter.escapeForLaTeX(modules.get(id).title());
            if (linkable.contains(id)) {
                return String.format("\\hyperref[sec:%s]{%s}", id, title);
            }
            return title;
        }
        return ModuleGuideLaTeXWriter.escapeForLaTeX(id);
    }

    private static List<String> lookupModules(
        final List<String> ids,
        final ModuleMap modules,
        final List<String> linkable
    ) {
        return ids.stream().map(id -> ModuleGuideLaTeXWriter.lookupModule(id, modules, linkable)).toList();
    }

    private static int[] toPagebreaks(final List<Integer> pagebreaks) {
        if (pagebreaks == null) {
            return new int[0];
        }
        final int[] result = new int[pagebreaks.size()];
        int index = 0;
        for (final Integer pagebreak : pagebreaks) {
            result[index++] = pagebreak;
        }
        return result;
    }

    private static void writeAuthors(final List<String> authors, final BufferedWriter writer) throws IOException {
        writer.write(authors.stream().map(ModuleGuideLaTeXWriter::formatAuthor).collect(Collectors.joining(", ")));
    }

    private static void writeDocumentEndStatic(final BufferedWriter writer) throws IOException {
        Main.newLine(writer);
        writer.write("\\end{document}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeDocumentStartStatic(final BufferedWriter writer) throws IOException {
        writer.write("\\documentclass[11pt]{book}");
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
        writer.write("\\usepackage{background}");
        Main.newLine(writer);
        writer.write("\\usepackage{tikz}");
        Main.newLine(writer);
        writer.write("\\usetikzlibrary{calc,positioning}");
        Main.newLine(writer);
        writer.write("\\usepackage{hyperref}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(
            "\\titleformat{\\chapter}[hang]{\\normalfont\\Huge\\bfseries\\color{fhdwblue}}{}{0em}{}"
        );
        Main.newLine(writer);
        writer.write(
            "\\titleformat{\\section}{\\normalfont\\LARGE\\color{fhdwblue}}{}{0em}{}"
        );
        Main.newLine(writer);
        writer.write("\\titleformat*{\\subsection}{\\normalfont\\large\\bfseries\\color{orange}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\titlespacing{\\chapter}{0pt}{*0}{*4}");
        Main.newLine(writer);
        writer.write("\\titlespacing{\\section}{0pt}{*0}{*4}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(
            "\\titlecontents{chapter}[0em]{\\vskip 0.5ex}{\\bfseries\\contentsmargin{0pt}}{}{\\titlerule*[3pt]{.}\\contentspage}"
        );
        Main.newLine(writer);
        writer.write(
            "\\titlecontents{section}[0em]{\\vskip 0.5ex}{\\contentsmargin{0pt}}{}{\\titlerule*[3pt]{.}\\contentspage}"
        );
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\csname @openrightfalse\\endcsname");
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
        writer.write("\\urlstyle{same}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\definecolor{fhdwblue}{RGB}{9,84,134}");
        Main.newLine(writer);
        writer.write("\\colorlet{fhdwyellow}{yellow!90!orange}");
        Main.newLine(writer);
        writer.write("\\colorlet{fhdworange}{orange!80!red}");
        Main.newLine(writer);
        writer.write("\\colorlet{mtabgray}{black!50}");
        Main.newLine(writer);
        writer.write("\\colorlet{mtabback}{black!10}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\backgroundsetup{");
        Main.newLine(writer);
        writer.write("scale=1,");
        Main.newLine(writer);
        writer.write("angle=0,");
        Main.newLine(writer);
        writer.write("opacity=1,");
        Main.newLine(writer);
        writer.write("contents={\\begin{tikzpicture}[overlay,remember picture]");
        Main.newLine(writer);
        writer.write("\\shade[top color=yellow, bottom color=orange] ");
        writer.write("($(current page.north east)+(-1,0)$) rectangle (current page.south east);");
        Main.newLine(writer);
        writer.write("\\end{tikzpicture}}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\fancyhead{}");
        Main.newLine(writer);
        writer.write("\\renewcommand{\\headrulewidth}{0pt}");
        Main.newLine(writer);
        writer.write("\\cfoot{\\thepage{}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{document}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeExaminationTypes(final BufferedWriter writer) throws IOException {
        writer.write("\\chapter{Prüfungsleistungen}\\label{chap:examinations}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Zu Anlage 1 gehört die folgende Legende, welche Art und Umfang der Prüfungsleistungen näher ");
        writer.write("erläutert:\\\\[3ex]");
        Main.newLine(writer);
        writer.write("\\begin{tikzpicture}[node distance=2 and 1]");
        Main.newLine(writer);
        writer.write("\\node (ex) {\\textbf{\\textcolor{fhdwblue}{PRÜFUNG}}};");
        Main.newLine(writer);
        writer.write("\\node (per) [right=0.2 of ex.south east, anchor=south west] ");
        writer.write("{\\textbf{\\textcolor{fhdwblue}{LEISTUNG}}};");
        Main.newLine(writer);
        writer.write("\\node (ex1) [below=of ex] {\\phantom{g}\\textbf{K}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per1) at (ex1 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write("Die Prüfung besteht aus einer 90-minütigen Klausur.");
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex2) [below=of ex1] {\\phantom{g}\\textbf{R}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per2) at (ex2 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write("Die Prüfung besteht aus einem Referat.");
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex3) [below=of ex2] {\\phantom{g}\\textbf{S}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per3) at (ex3 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write("Die Prüfung besteht aus einer Studienarbeit.");
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex4) [below=of ex3] {\\phantom{g}\\textbf{P}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per4) at (ex4 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write("Die Prüfung ist eine praktische Prüfung.");
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex5) [below=of ex4] {\\phantom{g}\\textbf{X}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per5) at (ex5 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write("Die Prüfung ist eine kombinierte Prüfung aus \\textbf{entweder} einer Klausur und einem ");
        writer.write("Referat \\textbf{oder} aus zwei Klausuren; der kombinierte Prüfungsumfang muss dabei dem einer ");
        writer.write("Einzelprüfung als Klausur oder Referat entsprechen (bei zwei Klausuren addieren sich ");
        writer.write("beispielsweise die Bearbeitungszeiten dieser beiden Klausuren zu 90 Minuten auf).");
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\coordinate (topright) at ($(per2.east |- ex.north)+(0.1,0.5)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (bottomleft) at ($(ex.west |- per5.south)+(-0.1,0.1)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (middle) at ($(ex.east)!0.5!(per.west)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (m1) at ($(ex.south)!0.5!(ex1.north)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (m2) at ($(ex1.south)!0.5!(ex2.north)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (m3) at ($(ex2.south)!0.5!(ex3.north)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (m4) at ($(ex3.south)!0.5!(ex4.north)$);");
        Main.newLine(writer);
        writer.write("\\coordinate (m5) at ($(ex4.south)!0.5!(ex5.north)$);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (topright -| bottomleft)");
        writer.write(" -- (topright)");
        writer.write(" -- (topright |- bottomleft)");
        writer.write(" -- (bottomleft)");
        writer.write(" -- cycle;");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (middle |- topright) -- (middle |- bottomleft);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (m1 -| bottomleft) -- (m1 -| topright);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (m2 -| bottomleft) -- (m2 -| topright);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (m3 -| bottomleft) -- (m3 -| topright);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (m4 -| bottomleft) -- (m4 -| topright);");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] (m5 -| bottomleft) -- (m5 -| topright);");
        Main.newLine(writer);
        writer.write("\\end{tikzpicture}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vspace*{3ex}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("Sind mehrere Prüfungsformen angegeben, stellt dies eine Auswahl aus den angegeben Alternativen ");
        writer.write("dar; nicht jedoch die Kombination dieser verschiedenen Prüfungsformen.");
        Main.newLine(writer);
        writer.write("Ist bei mehreren Prüfungsformen eine davon hervorgehoben, so wird diese bevorzugt.");
        Main.newLine(writer);
        writer.write("Die jeweils ausgewählte Prüfungsform wird zu Beginn der entsprechenden Veranstaltung ");
        writer.write("bekanntgegeben.");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeItemize(
        final List<String> items,
        final String noItems,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeItemize(items, noItems, "$\\bullet$", writer);
    }

    private static void writeItemize(
        final List<String> items,
        final String noItems,
        final String label,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\begin{itemize}[itemsep=0pt,topsep=0pt,label={");
        writer.write(label);
        writer.write("}]");
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

    private static void writeLiterature(
        final String title,
        final List<Source> literature,
        final BufferedWriter writer
    ) throws IOException {
        if (literature != null && !literature.isEmpty()) {
            writer.write("\\subsection*{");
            writer.write(title);
            writer.write("}");
            Main.newLine(writer);
            Main.newLine(writer);
            boolean first = true;
            for (final Source source : literature) {
                if (first) {
                    first = false;
                } else {
                    writer.write("\\\\[1.5ex]");
                    Main.newLine(writer);
                }
                ModuleGuideLaTeXWriter.writeSource(source, writer);
            }
            Main.newLine(writer);
            Main.newLine(writer);
        }
    }

    private static void writeLongtableHeader(
        final boolean withHeadings,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\begin{longtable}{|l|*{3}{C{1.1cm}|}C{1.6cm}|C{2.2cm}|}");
        Main.newLine(writer);
        writer.write("\\hline\\endhead");
        Main.newLine(writer);
        if (withHeadings) {
            writer.write("\\rule{0pt}{9mm}\\begin{minipage}{");
            writer.write(ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE);
            writer.write("}\\textbf{\\textcolor{fhdwblue}{MODUL}}\\end{minipage} & \\begin{minipage}{1cm}");
            writer.write("\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{SEMES\\-TER}}\\end{center}");
            writer.write("\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & \\begin{minipage}{1cm}");
            writer.write("\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}\\begin{center}");
            writer.write("\\textbf{\\textcolor{fhdwblue}{KONTAKT\\-STUNDEN}}\\end{center}");
            writer.write("\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & \\begin{minipage}{1cm}");
            writer.write("\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}\\begin{center}");
            writer.write("\\textbf{\\textcolor{fhdwblue}{SELBST\\-STUDIUM}}\\end{center}");
            writer.write("\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & \\begin{minipage}{1.5cm}");
            writer.write("\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}\\begin{center}");
            writer.write("\\textbf{\\textcolor{fhdwblue}{CREDIT POINTS (ECTS)}}\\end{center}");
            writer.write("\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & \\begin{minipage}{2.1cm}");
            writer.write("\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}\\begin{center}");
            writer.write("\\textbf{\\textcolor{fhdwblue}{PRÜ\\-FUNGS\\-FORM}}");
            writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage}\\\\\\hline");
            Main.newLine(writer);
        }
    }

    private static void writeModule(
        final MetaModule meta,
        final ModuleMap modules,
        final int weightSum,
        final List<String> linkable,
        final BufferedWriter writer
    ) throws IOException {
        final Module module = modules.get(meta.module());
        if (module == null) {
            System.out.println(meta.module());
            return;
        }
        final List<String[]> table = new LinkedList<String[]>();
        table.add(new String[] {"Kürzel", meta.module()});
        table.add(new String[] {"Lehrsprache", ModuleGuideLaTeXWriter.escapeForLaTeX(module.language())});
        table.add(new String[] {"ECTS-Punkte", String.valueOf(module.ects())});
        table.add(new String[] {"Kontaktstunden", String.valueOf(module.contacthours())});
        table.add(new String[] {"Selbststudium", String.valueOf(module.homehours())});
        table.add(new String[] {"Dauer", meta.duration() + " Semester"});
        table.add(new String[] {"Häufigkeit", ModuleGuideLaTeXWriter.escapeForLaTeX(meta.frequency())});
        table.add(new String[] {"Prüfungsleistung", ModuleGuideLaTeXWriter.formatExamination(module.examination())});
        writer.write("\\section{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(module.title()));
        writer.write("}\\label{sec:");
        writer.write(meta.module());
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Allgemeine Angaben}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{tabularx}{\\textwidth}");
        writer.write("{!{\\color{mtabgray}\\vrule}l!{\\color{mtabgray}\\vrule}X!{\\color{mtabgray}\\vrule}}");
        Main.newLine(writer);
        writer.write("\\arrayrulecolor{mtabgray}\\hline");
        Main.newLine(writer);
        boolean odd = true;
        for (final String[] line : table) {
            if (odd) {
                writer.write("\\rowcolor{mtabback}");
                Main.newLine(writer);
            }
            odd = !odd;
            writer.write("\\textbf{");
            writer.write(line[0]);
            writer.write("} & ");
            writer.write(line[1]);
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
            ModuleGuideLaTeXWriter.writeItemize(
                module.keywords().stream().map(ModuleGuideLaTeXWriter::escapeForLaTeX).toList(),
                "Keine",
                writer
            );
        }
        writer.write("\\subsection*{Zugangsvoraussetzungen}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeItemize(
            ModuleGuideLaTeXWriter.lookupModules(module.preconditions(), modules, linkable),
            "Keine",
            writer
        );
        writer.write("\\subsection*{Zugangsempfehlungen}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeItemize(
            ModuleGuideLaTeXWriter.lookupModules(module.recommendations(), modules, linkable),
            "Keine",
            writer
        );
        writer.write("\\subsection*{Qualifikations- und Kompetenzziele}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeText(module.competencies(), writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Lehr- und Lernmethoden}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.teachingmethods().isEmpty()) {
            writer.write("Präsenzveranstaltungen, Eigenstudium, individuelles und kooperatives Lernen, ");
            writer.write("problemorientiertes und integratives Lernen, forschendes Lernen, synchrones und ");
            writer.write("asynchrones Lernen, Übungen, Fallstudien, Expertenvorträge, Projekte, Gruppenarbeit.");
            Main.newLine(writer);
        } else {
            ModuleGuideLaTeXWriter.writeText(module.teachingmethods(), writer);
        }
        Main.newLine(writer);
        if (module.special() != null && !module.special().isEmpty()) {
            writer.write("\\subsection*{Besonderheiten}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleGuideLaTeXWriter.writeText(module.special(), writer);
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
            ModuleGuideLaTeXWriter.writeItemize(
                module.content().stream().map(ModuleGuideLaTeXWriter::chapterToItem).toList(),
                "keine",
                writer
            );
        }
        ModuleGuideLaTeXWriter.writeLiterature("Grundlegende Literaturhinweise", module.requiredliterature(), writer);
        ModuleGuideLaTeXWriter.writeLiterature("Ergänzende Literaturhinweise", module.optionalliterature(), writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writePagebreakForLongtable(final BufferedWriter writer) throws IOException {
        writer.write("\\end{longtable}");
        Main.newLine(writer);
        writer.write("\\pagebreak{}");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeLongtableHeader(false, writer);
    }

    private static void writeSource(final Source source, final BufferedWriter writer) throws IOException {
        if (source.type() == SourceType.HINT) {
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.title()));
            return;
        }
        writer.write("\\begin{minipage}{\\textwidth}");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeAuthors(source.authors(), writer);
        if (source.year() != null) {
            writer.write(", ");
            writer.write(String.valueOf(source.year()));
        }
        writer.write(". \\textit{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.title()));
        writer.write("}");
        switch (source.type()) {
        case BOOK:
            if (source.edition() != null) {
                writer.write(", ");
                writer.write(String.valueOf(source.edition()));
                writer.write(".~Auflage. ");
            } else {
                writer.write(".");
            }
            break;
        case ARTICLE:
            final boolean hasJournal = source.journal() != null && !source.journal().isBlank();
            if (hasJournal) {
                writer.write(", ");
                writer.write(source.journal());
            }
            final boolean hasVolume = source.volume() != null && !source.volume().isBlank();
            if (hasVolume) {
                writer.write(", Ausgabe ");
                writer.write(source.volume());
            }
            final boolean hasNumber = source.number() != null && !source.number().isBlank();
            if (hasNumber) {
                writer.write(", Nummer ");
                writer.write(source.number());
            }
            if (source.frompage() != null) {
                writer.write(", S. ");
                writer.write(String.valueOf(source.frompage()));
                if (source.topage() != null) {
                    writer.write("--");
                    writer.write(String.valueOf(source.topage()));
                }
            }
            if (hasJournal || hasVolume || hasNumber) {
                writer.write(".");
            }
            break;
        default:
            writer.write(".");
        }
        final boolean hasLocation = source.location() != null && !source.location().isBlank();
        if (hasLocation) {
            writer.write(" ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.location()));
            writer.write(":");
        }
        if (source.publisher() != null && !source.publisher().isBlank()) {
            writer.write(" ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.publisher()));
            writer.write(".");
        }
        if (source.isbn() != null && !source.isbn().isBlank()) {
            writer.write(" ISBN: ");
            writer.write(source.isbn());
            writer.write(".");
        }
        if (source.doi() != null && !source.doi().isBlank()) {
            writer.write(" DOI: ");
            writer.write(source.doi());
            writer.write(".");
        }
        Main.newLine(writer);
        writer.write("\\end{minipage}");
    }

    private static void writeStats(final ModuleStats stats, final BufferedWriter writer) throws IOException {
        writer.write("\\begin{minipage}{");
        writer.write(ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE);
        writer.write("}\\raggedright\\strut{}\\hyperref[sec:");
        writer.write(stats.id());
        writer.write("]{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(stats.title()));
        writer.write("}\\strut{}\\end{minipage} & ");
        writer.write(String.valueOf(stats.semester()));
        if (stats.duration() > 1) {
            writer.write("--");
            writer.write(String.valueOf(stats.semester() + stats.duration() - 1));
        }
        writer.write(" & ");
        writer.write(String.valueOf(stats.contactHours()));
        writer.write(" & ");
        writer.write(String.valueOf(stats.homeHours()));
        writer.write(" & ");
        writer.write(String.valueOf(stats.ects()));
        writer.write(" & ");
        writer.write(ModuleGuideLaTeXWriter.formatExamination(stats.examination()));
        writer.write("\\\\\\hline");
        Main.newLine(writer);
    }

    private static void writeText(final List<String> sentences, final BufferedWriter writer) throws IOException {
        writer.write(
            sentences
            .stream()
            .map(ModuleGuideLaTeXWriter::escapeForLaTeX)
            .collect(Collectors.joining(Main.lineSeparator))
        );
        Main.newLine(writer);
    }

    public ModuleGuideLaTeXWriter(final ModuleGuide guide, final ModuleMap modules) {
        super(guide, modules);
    }

    @Override
    protected void writeDocumentEnd(final BufferedWriter writer) throws IOException {
        ModuleGuideLaTeXWriter.writeDocumentEndStatic(writer);
    }

    @Override
    protected void writeDocumentStart(final BufferedWriter writer) throws IOException {
        ModuleGuideLaTeXWriter.writeDocumentStartStatic(writer);
    }

    @Override
    protected void writeIntro(final ModuleGuide guide, final BufferedWriter writer) throws IOException {
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
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.degree().substring(0, guide.degree().indexOf(' '))));
        writer.write("-Studiengang ");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.subject()));
        writer.write(" im Studienjahr ");
        writer.write(guide.year());
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
        writer.write("\\vfill");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("{\\Huge \\textbf{\\textcolor{orange}{\\url{www.fhdw.de}}}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeModules(
        final ModuleGuide guide,
        final ModuleMap modules,
        final int weightSum,
        final BufferedWriter writer
    ) throws IOException {
        final List<String> linkable = guide.modules().stream().map(MetaModule::module).toList();
        String specialization = "";
        int semester = 0;
        for (final MetaModule meta : guide.modules().stream().sorted().toList()) {
            if (meta.specialization() != null && !specialization.equals(meta.specialization())) {
                specialization = meta.specialization();
                writer.write("\\chapter{Spezialisierung ");
                writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(specialization));
                writer.write("}");
                Main.newLine(writer);
                Main.newLine(writer);
            } else if (
                (meta.specialization() == null || meta.specialization().isBlank()) && meta.semester() != semester
            ) {
                semester = meta.semester();
                writer.write("\\chapter{");
                writer.write(String.valueOf(semester));
                writer.write(". Semester}");
                Main.newLine(writer);
                Main.newLine(writer);
            }
            ModuleGuideLaTeXWriter.writeModule(meta, modules, weightSum, linkable, writer);
        }
    }

    @Override
    protected void writeOverview(
        final ModuleGuide guide,
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\tableofcontents");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeExaminationTypes(writer);
        writer.write("\\chapter{Studienplan -- Lehrveranstaltungen}\\label{chap:studienplan}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{tikzpicture}");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (subject) {\\large\\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.subject().toUpperCase()));
        writer.write("}};");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (degree) [below=0.5 of subject.west, anchor=west] {\\large ");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.degree()));
        writer.write("};");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (time) [below=0.5 of degree.west, anchor=west] {\\large ");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.timemodel()));
        writer.write("studium};");
        Main.newLine(writer);
        writer.write("\\draw[fhdwblue,thick] ($(subject.west)+(-0.1,0.2)$) -- ($(time.west)+(-0.1,-0.2)$);");
        Main.newLine(writer);
        writer.write("\\end{tikzpicture}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vspace*{3ex}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\arrayrulecolor{fhdwblue}");
        Main.newLine(writer);
        writer.write("\\renewcommand{\\arraystretch}{1.5}");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeLongtableHeader(true, writer);
        int semester = 1;
        int groupsOnPage = 0;
        int pagebreakIndex = 0;
        int numberOfSpecializationModules = 0;
        final int[] pagebreaks = ModuleGuideLaTeXWriter.toPagebreaks(guide.pagebreaks());
        for (final List<ModuleStats> modules : overview.semesters()) {
            if (pagebreakIndex < pagebreaks.length && groupsOnPage >= pagebreaks[pagebreakIndex]) {
                ModuleGuideLaTeXWriter.writePagebreakForLongtable(writer);
                groupsOnPage = 0;
                pagebreakIndex++;
            }
            writer.write("\\rowcolor{fhdwblue}\\multicolumn{6}{c}{\\textcolor{white}{");
            writer.write(String.valueOf(semester));
            writer.write(". Semester}}\\\\\\hline");
            Main.newLine(writer);
            for (final ModuleStats stats : modules) {
                ModuleGuideLaTeXWriter.writeStats(stats, writer);
                if (ModuleStats.SEE_SPECIALIZATION.equals(stats.examination())) {
                    numberOfSpecializationModules++;
                }
            }
            semester++;
            groupsOnPage++;
        }
        writer.write("\\rowcolor{fhdwblue}\\begin{minipage}{");
        writer.write(ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE);
        writer.write("}\\textcolor{white}{Summe}\\end{minipage} &  & ");
        writer.write("\\textcolor{white}{");
        writer.write(String.valueOf(overview.contactHoursSum()));
        writer.write("} & \\textcolor{white}{");
        writer.write(String.valueOf(overview.homeHoursSum()));
        writer.write("} & \\textcolor{white}{");
        writer.write(String.valueOf(overview.ectsSum()));
        writer.write("} & \\\\\\hline");
        Main.newLine(writer);
        if (!overview.specializations().isEmpty()) {
            writer.write("\\end{longtable}");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\clearpage");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\chapter{Spezialisierungsbereiche mit jeweils den Modulen I bis ");
            writer.write(ModuleStats.toRomanNumeral(numberOfSpecializationModules));
            writer.write("}\\label{chap:specialareas}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleGuideLaTeXWriter.writeLongtableHeader(true, writer);
            for (final Map.Entry<String, List<ModuleStats>> entry : overview.specializations().entrySet()) {
                if (pagebreakIndex < pagebreaks.length && groupsOnPage >= pagebreaks[pagebreakIndex]) {
                    ModuleGuideLaTeXWriter.writePagebreakForLongtable(writer);
                    groupsOnPage = 0;
                    pagebreakIndex++;
                }
                writer.write("\\rowcolor{fhdwblue}\\multicolumn{6}{c}{\\textcolor{white}{");
                writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(entry.getKey()));
                writer.write("}}\\\\\\hline");
                Main.newLine(writer);
                for (final ModuleStats stats : entry.getValue()) {
                    ModuleGuideLaTeXWriter.writeStats(stats, writer);
                }
                groupsOnPage++;
            }
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
    protected void writeTitlePage(final ModuleGuide guide, final BufferedWriter writer) throws IOException {
        writer.write("\\pagestyle{empty}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\NoBgThispage");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{tikzpicture}[overlay,remember picture]");
        Main.newLine(writer);
        writer.write("\\node at (current page.center) {\\includegraphics[height=\\paperheight]{frontpage.png}};");
        Main.newLine(writer);
        writer.write("\\end{tikzpicture}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vspace*{15cm}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("{\\color{white}");
        Main.newLine(writer);
        writer.write("\\Large Modulhandbuch ");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.timemodel()));
        writer.write("\\\\[1ex]");
        Main.newLine(writer);
        writer.write("\\Huge \\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.subject()));
        writer.write("}\\\\[0.5ex]");
        Main.newLine(writer);
        writer.write("\\Huge \\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(guide.degree()));
        writer.write("}\\\\[0.5ex]");
        Main.newLine(writer);
        writer.write("\\Large Studienjahr ");
        writer.write(guide.year());
        Main.newLine(writer);
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\vfill");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("{\\Huge \\textbf{\\textcolor{orange}{\\url{www.fhdw.de}}}}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

}
