package moduleguidehelper;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.stream.*;

import com.google.gson.*;

import moduleguidehelper.internationalization.*;
import moduleguidehelper.model.*;
import moduleguidehelper.model.Module;

public class ModuleGuideLaTeXWriter extends ModuleGuideWriter {

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\$\\$[^\\$]+\\$\\$");

    private static final int MAX_NUMBER_OF_AUTHORS = 3;

    private static final String OVERVIEW_FIRST_COL_SIZE = "7.2cm";

    private static final String OVERVIEW_FIRST_COL_SIZE_ELECTIVE = "5.7cm";

    public static void writeModule(
        final String id,
        final RawModule module,
        final int weightSum,
        final String modulesFolder,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeDocumentStartStatic(writer);
        writer.write("\\pagestyle{fancy}");
        Main.newLine(writer);
        Main.newLine(writer);
        final ModuleMap modules = new ModuleMap();
        modules.put(id, module);
        ModuleGuideLaTeXWriter.writeModule(
            new Module(
                new MetaModule(id, 1, 1, "Pflicht", "jedes Jahr", 5, 1, "", "", "", "", null),
                module
            ),
            weightSum,
            List.of(),
            modulesFolder,
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
                chapter.sections(),
                "",
                "$\\circ$",
                true,
                buffer
            );
            buffer.flush();
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String computeNumbersForElectiveStats(final ModuleStats stats, final ModuleGuide guide) {
        final TreeSet<Integer> numbers =
            guide
            .modules()
            .stream()
            .filter(
                module -> module.meta().module().equals(stats.id())
                && Main.ELECTIVE.equals(module.meta().specialization())
            ).map(module -> module.meta().specializationnumber())
            .collect(Collectors.toCollection(TreeSet::new));
        if (numbers.size() == 1) {
            return ModuleStats.toRomanNumeral(numbers.first());
        }
        int previous = numbers.first() - 1;
        boolean consecutive = true;
        for (final Integer number : numbers) {
            if (number - 1 == previous) {
                previous = number;
            } else {
                consecutive = false;
                break;
            }
        }
        if (consecutive) {
            return String.format(
                "%s--%s",
                ModuleStats.toRomanNumeral(numbers.first()),
                ModuleStats.toRomanNumeral(numbers.last())
            );
        }
        return numbers.stream().map(ModuleStats::toRomanNumeral).collect(Collectors.joining(", "));
    }

    private static String escapeForLaTeX(final String text) {
        if (text == null) {
            return "";
        }
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

    private static String formatExamination(final String examination, final Internationalization internationalization) {
        if (examination == null || examination.isBlank()) {
            return "\\textbf{\\textcolor{red}{ANGABEN FEHLEN!}}";
        }
        final ExaminationTypes types = ExaminationType.parse(examination);
        if (types != null) {
            if (types.types().size() == 1) {
                return String.format(
                    "\\textbf{%s}",
                    types.types().iterator().next().toString(internationalization)
                );
            }
            return types
                .types()
                .stream()
                .map(
                    type -> types.preferred().contains(type) ?
                        String.format("\\textbf{%s}", type.toString(internationalization)) :
                            type.toString(internationalization)
                ).collect(Collectors.joining());
        }
        return examination;
    }

    private static boolean isSet(final String text) {
        return text != null && !text.isBlank();
    }

    private static String lookupModule(
        final String id,
        final String modulesFolder,
        final List<String> linkable
    ) {
        final File json = new File(modulesFolder + "/" + id.toLowerCase() + ".json");
        if (json.exists()) {
            final RawModule raw;
            try (FileReader reader = new FileReader(json)) {
                raw = Main.GSON.fromJson(reader, RawModule.class);
            } catch (final IOException | JsonSyntaxException e) {
                return ModuleGuideLaTeXWriter.escapeForLaTeX(id);
            }
            final String title = ModuleGuideLaTeXWriter.escapeForLaTeX(raw.title());
            if (linkable.contains(id)) {
                return String.format("\\hyperref[sec:%s]{%s}", id, title);
            }
            return title;
        } else {
            Main.LOGGER.log(Level.WARNING, "Lookup failed for: " + id);
        }
        return ModuleGuideLaTeXWriter.escapeForLaTeX(id);
    }

    private static List<String> lookupModules(
        final List<String> ids,
        final String modulesFolder,
        final List<String> linkable
    ) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(id -> ModuleGuideLaTeXWriter.lookupModule(id, modulesFolder, linkable)).toList();
    }

    private static List<List<Chapter>> separateContentsByHints(final List<Chapter> content) {
        final List<List<Chapter>> result = new LinkedList<List<Chapter>>();
        List<Chapter> currentContents = new LinkedList<Chapter>();
        for (final Chapter chapter : content) {
            if (chapter.chapter().startsWith("!")) {
                if (!currentContents.isEmpty()) {
                    result.add(currentContents);
                    currentContents = new LinkedList<Chapter>();
                }
                result.add(List.of(chapter));
            } else {
                currentContents.add(chapter);
            }
        }
        if (!currentContents.isEmpty()) {
            result.add(currentContents);
        }
        return result;
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

    private static void writeAuthors(
        final List<String> authors,
        final List<String> editors,
        final String institution,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        if (authors == null || authors.isEmpty()) {
            if (editors == null || editors.isEmpty()) {
                if (ModuleGuideLaTeXWriter.isSet(institution)) {
                    writer.write(institution);
                }
                return;
            }
            writer.write(
                editors
                .stream()
                .limit(ModuleGuideLaTeXWriter.MAX_NUMBER_OF_AUTHORS)
                .map(ModuleGuideLaTeXWriter::formatAuthor)
                .collect(Collectors.joining(", "))
            );
            if (editors.size() > ModuleGuideLaTeXWriter.MAX_NUMBER_OF_AUTHORS) {
                writer.write(" et al.");
            }
            writer.write(" (");
            writer.write(
                internationalization.internationalize(
                    editors.size() == 1 ? InternationalizationKey.EDITOR_ABBR : InternationalizationKey.EDITORS_ABBR
                )
            );
            writer.write(")");
        } else {
            writer.write(
                authors
                .stream()
                .limit(ModuleGuideLaTeXWriter.MAX_NUMBER_OF_AUTHORS)
                .map(ModuleGuideLaTeXWriter::formatAuthor)
                .collect(Collectors.joining(", "))
            );
            if (authors.size() > ModuleGuideLaTeXWriter.MAX_NUMBER_OF_AUTHORS) {
                writer.write(" et al.");
            }
        }
    }

    private static void writeCommaSeparated(final List<String> items, final BufferedWriter writer) throws IOException {
        writer.write(
            items
            .stream()
            .map(ModuleGuideLaTeXWriter::escapeForLaTeX)
            .collect(Collectors.joining(", "))
        );
        Main.newLine(writer);
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
        writer.write("\\pdfinfoomitdate 1");
        Main.newLine(writer);
        writer.write("\\pdftrailerid{}");
        Main.newLine(writer);
        writer.write("\\pdfsuppressptexinfo=-1");
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
        writer.write("\\titlecontents{chapter}[0em]{\\vskip 1.5ex}{\\bfseries\\contentsmargin{0pt}}{}");
        writer.write("{\\titlerule*[3pt]{.}\\contentspage}");
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
        writer.write("\\newcommand{\\llb}{\\\\}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{document}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeExaminationTypes(
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\chapter{");
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS));
        writer.write("}\\label{chap:examinations}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_INTRO));
        writer.write(":\\\\[3ex]");
        Main.newLine(writer);
        writer.write("\\begin{tikzpicture}[node distance=2 and 1]");
        Main.newLine(writer);
        writer.write("\\node (ex) {\\textbf{\\textcolor{fhdwblue}{");
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_HEADER1));
        writer.write("}}};");
        Main.newLine(writer);
        writer.write("\\node (per) [right=0.2 of ex.south east, anchor=south west] ");
        writer.write("{\\textbf{\\textcolor{fhdwblue}{");
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_HEADER2));
        writer.write("}}};");
        Main.newLine(writer);
        writer.write("\\node (ex1) [below=of ex] {\\phantom{g}\\textbf{K}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per1) at (ex1 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write(internationalization.internationalize(InternationalizationKey.EXAM_FORM));
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex2) [below=of ex1] {\\phantom{g}\\textbf{R}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per2) at (ex2 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write(internationalization.internationalize(InternationalizationKey.PRESENTATION_FORM));
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex3) [below=of ex2] {\\phantom{g}\\textbf{S}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per3) at (ex3 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write(internationalization.internationalize(InternationalizationKey.PAPER_FORM));
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex4) [below=of ex3] {\\phantom{g}\\textbf{P}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per4) at (ex4 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write(internationalization.internationalize(InternationalizationKey.PRACTICAL_FORM));
        writer.write("\\strut{}\\end{minipage}};");
        Main.newLine(writer);
        writer.write("\\node (ex5) [below=of ex4] {\\phantom{g}\\textbf{X}\\phantom{g}};");
        Main.newLine(writer);
        writer.write("\\node (per5) at (ex5 -| per.west) [anchor=west] ");
        writer.write("{\\begin{minipage}{14cm}\\raggedright\\strut{}");
        writer.write(internationalization.internationalize(InternationalizationKey.PORTFOLIO_FORM));
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
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_MULTIPLE));
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_PREFERRED));
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATIONS_ANNOUNCEMENT));
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeGeneralModuleInformationSection(
        final Module module,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        final List<String[]> table = new LinkedList<String[]>();
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.ID),
                ModuleGuideLaTeXWriter.escapeForLaTeX(module.meta().module())
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.RESPONSIBLE),
                module.module().responsible()
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.TEACHING_LANGUAGE),
                ModuleGuideLaTeXWriter.escapeForLaTeX(
                    module.module().teachinglanguage().toString(module.module().descriptionlanguage())
                )
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.ECTS),
                String.valueOf(module.module().ects())
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.CONTACT_HOURS),
                String.valueOf(module.module().contacthours())
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.SELF_STUDY),
                String.valueOf(module.module().homehours())
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.DURATION),
                String.format(
                    "%d %s",
                    module.meta().duration(),
                    internationalization.internationalize(InternationalizationKey.SEMESTER)
                )
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.FREQUENCY),
                ModuleGuideLaTeXWriter.escapeForLaTeX(module.meta().frequency())
            }
        );
        table.add(
            new String[] {
                internationalization.internationalize(InternationalizationKey.EXAMINATION),
                ModuleGuideLaTeXWriter.formatExamination(module.module().examination(), internationalization)}
            );
        writer.write("\\subsection*{");
        writer.write(internationalization.internationalize(InternationalizationKey.GENERAL_INFORMATION));
        writer.write("}");
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
    }

    private static void writeItemize(
        final List<String> items,
        final String noItems,
        final boolean escape,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeItemize(items, noItems, "$\\bullet$", escape, writer);
    }

    private static void writeItemize(
        final List<String> items,
        final String noItems,
        final String label,
        final boolean escape,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\begin{itemize}[itemsep=0pt,topsep=0pt,label={");
        writer.write(label);
        writer.write("}]");
        Main.newLine(writer);
        if (items == null || items.isEmpty()) {
            writer.write("\\item ");
            writer.write(noItems);
            Main.newLine(writer);
        } else {
            for (final String item : items) {
                writer.write("\\item ");
                if (escape) {
                    writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(item));
                } else {
                    writer.write(item);
                }
                Main.newLine(writer);
            }
        }
        writer.write("\\end{itemize}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeItemizeSection(
        final String section,
        final List<String> items,
        final String noItems,
        final BufferedWriter writer
    ) throws IOException {
        if (items != null && !items.isEmpty()) {
            writer.write("\\subsection*{");
            writer.write(section);
            writer.write("}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleGuideLaTeXWriter.writeItemize(items, noItems, true, writer);
        }
    }

    private static void writeLiteratureSection(
        final String title,
        final List<Source> literature,
        final Internationalization internationalization,
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
                ModuleGuideLaTeXWriter.writeSource(source, internationalization, writer);
            }
            Main.newLine(writer);
            Main.newLine(writer);
        }
    }

    private static void writeLongtableHeader(
        final boolean withHeadings,
        final boolean elective,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write(String.format("\\begin{longtable}{|l|*{%d}{C{1.1cm}|}C{1.6cm}|C{2.2cm}|}", elective ? 4 : 3));
        Main.newLine(writer);
        writer.write("\\hline\\endhead");
        Main.newLine(writer);
        if (withHeadings) {
            writer.write("\\rule{0pt}{9mm}\\begin{minipage}{");
            writer.write(
                elective ?
                    ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE_ELECTIVE :
                        ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE
            );
            writer.write("}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.MODULE_HEADER));
            writer.write("}}\\end{minipage} & ");
            if (elective) {
                writer.write("\\begin{minipage}{1cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
                writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
                writer.write(internationalization.internationalize(InternationalizationKey.ELECTIVE_HEADER));
                writer.write("}}\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & ");
            }
            writer.write("\\begin{minipage}{1cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.SEMESTER_HEADER));
            writer.write("}}\\end{center}");
            writer.write("\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & ");
            writer.write("\\begin{minipage}{1cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.CONTACT_HOURS_HEADER));
            writer.write("}}\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & ");
            writer.write("\\begin{minipage}{1cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.SELF_STUDY_HEADER));
            writer.write("}}\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & ");
            writer.write("\\begin{minipage}{1.5cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.ECTS_HEADER));
            writer.write("}}\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage} & ");
            writer.write("\\begin{minipage}{2.1cm}\\begin{center}\\rotatebox{270}{\\begin{minipage}{2cm}");
            writer.write("\\begin{center}\\textbf{\\textcolor{fhdwblue}{");
            writer.write(internationalization.internationalize(InternationalizationKey.EXAMINATION_HEADER));
            writer.write("}}");
            writer.write("\\end{center}\\end{minipage}}\\\\[1mm]\\end{center}\\end{minipage}\\\\\\hline");
            Main.newLine(writer);
        }
    }

    private static void writeLongtableHeader(
        final boolean withHeadings,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeLongtableHeader(withHeadings, false, internationalization, writer);
    }

    private static void writeLookupSection(
        final String section,
        final List<String> items,
        final String modulesFolder,
        final List<String> linkable,
        final String none,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\subsection*{");
        writer.write(section);
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeItemize(
            ModuleGuideLaTeXWriter.lookupModules(items, modulesFolder, linkable),
            none,
            false,
            writer
        );
    }

    private static void writeModule(
        final Module module,
        final int weightSum,
        final List<String> linkable,
        final String modulesFolder,
        final BufferedWriter writer
    ) throws IOException {
        try {
            if (module.module() == null) {
                Main.LOGGER.log(Level.SEVERE, module.meta().module());
                return;
            }
            if (module.module().descriptionlanguage() == null) {
                Main.LOGGER.log(
                    Level.SEVERE,
                    String.format("Description language is missing in module %s!", module.meta().module())
                );
                return;
            }
            final Internationalization internationalization =
                module.module().descriptionlanguage().getInternationalization();
            final String none = internationalization.internationalize(InternationalizationKey.NONE);
            ModuleGuideLaTeXWriter.writeModuleTitle(module, writer);
            ModuleGuideLaTeXWriter.writeGeneralModuleInformationSection(module, internationalization, writer);
            ModuleGuideLaTeXWriter.writeItemizeSection(
                internationalization.internationalize(InternationalizationKey.KEYWORDS),
                module.module().keywords(),
                none,
                writer
            );
            ModuleGuideLaTeXWriter.writeLookupSection(
                internationalization.internationalize(InternationalizationKey.REQUIREMENTS),
                module.module().preconditions(),
                modulesFolder,
                linkable,
                none,
                writer
            );
            ModuleGuideLaTeXWriter.writeLookupSection(
                internationalization.internationalize(InternationalizationKey.RECOMMENDATIONS),
                module.module().recommendations(),
                modulesFolder,
                linkable,
                none,
                writer
            );
            ModuleGuideLaTeXWriter.writeModuleQualificationSection(module, internationalization, writer);
            ModuleGuideLaTeXWriter.writeModuleTeachingMethodsSection(module, internationalization, writer);
            ModuleGuideLaTeXWriter.writeModuleContentsSection(module, internationalization, writer);
            ModuleGuideLaTeXWriter.writeLiteratureSection(
                internationalization.internationalize(InternationalizationKey.REQUIRED_LITERATURE),
                module.module().requiredliterature(),
                internationalization,
                writer
            );
            ModuleGuideLaTeXWriter.writeLiteratureSection(
                internationalization.internationalize(InternationalizationKey.RECOMMENDED_LITERATURE),
                module.module().optionalliterature(),
                internationalization,
                writer
            );
            writer.write("\\clearpage");
            Main.newLine(writer);
            Main.newLine(writer);
        } catch (RuntimeException | IOException e) {
            Main.LOGGER.log(
                Level.SEVERE,
                String.format("Exception in module %s: %s", module.meta().module(), e.getMessage())
            );
            throw e;
        }
    }

    private static void writeModuleContentsSection(
        final Module module,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\subsection*{");
        writer.write(internationalization.internationalize(InternationalizationKey.CONTENTS));
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.module().content() != null) {
            final List<List<Chapter>> separatedContents =
                ModuleGuideLaTeXWriter.separateContentsByHints(module.module().content());
            for (final List<Chapter> contents : separatedContents) {
                if (contents.getFirst().chapter().startsWith("!")) {
                    writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(contents.getFirst().chapter().substring(1)));
                    Main.newLine(writer);
                    Main.newLine(writer);
                } else {
                    ModuleGuideLaTeXWriter.writeItemize(
                        contents.stream().map(ModuleGuideLaTeXWriter::chapterToItem).toList(),
                        internationalization.internationalize(InternationalizationKey.NONE),
                        false,
                        writer
                    );
                }
            }
        }
    }

    private static void writeModuleQualificationSection(
        final Module module,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\subsection*{");
        writer.write(internationalization.internationalize(InternationalizationKey.QUALIFICATION));
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.module().competenciespreface() != null && !module.module().competenciespreface().isBlank()) {
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(module.module().competenciespreface()));
            Main.newLine(writer);
        }
        writer.write(internationalization.internationalize(InternationalizationKey.QUALIFICATION_START));
        writer.write("\\\\");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeItemize(module.module().competencies(), "", true, writer);
        Main.newLine(writer);
    }

    private static void writeModuleTeachingMethodsSection(
        final Module module,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        if (
            (module.module().teachingmethods() == null || module.module().teachingmethods().isEmpty())
            && (module.module().teachingpostface() == null || module.module().teachingpostface().isEmpty())
        ) {
            return;
        }
        writer.write("\\subsection*{");
        writer.write(internationalization.internationalize(InternationalizationKey.TEACHING_METHODS));
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
        if (module.module().teachingmethods() != null) {
            ModuleGuideLaTeXWriter.writeCommaSeparated(
                module
                .module()
                .teachingmethods()
                .stream()
                .map(text ->
                    "DEFAULT".equals(text) ?
                        internationalization.internationalize(InternationalizationKey.DEFAULT_TEACHING) :
                            text
                ).toList(),
                writer
            );
        }
        if (module.module().teachingpostface() != null && !module.module().teachingpostface().isBlank()) {
            writer.write("\\\\");
            Main.newLine(writer);
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(module.module().teachingpostface()));
        }
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writeModuleTitle(final Module module, final BufferedWriter writer) throws IOException {
        writer.write("\\section{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(module.module().title()));
        writer.write("}\\label{sec:");
        writer.write(module.meta().module());
        writer.write("}");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    private static void writePagebreakForLongtable(
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\end{longtable}");
        Main.newLine(writer);
        writer.write("\\pagebreak{}");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeLongtableHeader(false, internationalization, writer);
    }

    private static void writeSource(
        final Source source,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        if (source == null) {
            return;
        }
        if (source.type() == SourceType.HINT) {
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.title()));
            return;
        }
        writer.write("\\begin{minipage}{\\textwidth}");
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeAuthors(
            source.authors(),
            source.editors(),
            source.institution(),
            internationalization,
            writer
        );
        if (source.year() != null) {
            writer.write(", ");
            writer.write(String.valueOf(source.year()));
        }
        writer.write(". \\textit{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.title()));
        if (ModuleGuideLaTeXWriter.isSet(source.subtitle())) {
            writer.write(": ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.subtitle()));
        }
        writer.write("}");
        switch (source.type()) {
        case BOOK:
            if (source.edition() != null && source.edition() > 0) {
                writer.write(", ");
                writer.write(internationalization.enumerate(source.edition()));
                writer.write("~");
                writer.write(internationalization.internationalize(InternationalizationKey.EDITION));
                writer.write(". ");
            } else {
                writer.write(".");
            }
            break;
        case ARTICLE:
            final boolean hasJournal = ModuleGuideLaTeXWriter.isSet(source.journal());
            if (hasJournal) {
                writer.write(", ");
                writer.write(source.journal());
            }
            final boolean hasVolume = ModuleGuideLaTeXWriter.isSet(source.volume());
            if (hasVolume) {
                writer.write(", ");
                writer.write(internationalization.internationalize(InternationalizationKey.VOLUME));
                writer.write(" ");
                writer.write(source.volume());
            }
            final boolean hasNumber = ModuleGuideLaTeXWriter.isSet(source.number());
            if (hasNumber) {
                writer.write(", ");
                writer.write(internationalization.internationalize(InternationalizationKey.NUMBER));
                writer.write(" ");
                writer.write(source.number());
            }
            if (source.frompage() != null) {
                final boolean multiplePages = source.topage() != null;
                writer.write(", ");
                writer.write(
                    internationalization.internationalize(
                        multiplePages ?
                            InternationalizationKey.PAGE_ABBR_PLURAL :
                                InternationalizationKey.PAGE_ABBR_SINGULAR
                    )
                );
                writer.write(". ");
                writer.write(String.valueOf(source.frompage()));
                if (multiplePages) {
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
        final boolean hasPublisher = ModuleGuideLaTeXWriter.isSet(source.publisher());
        if (ModuleGuideLaTeXWriter.isSet(source.location())) {
            writer.write(" ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.location()));
            writer.write(hasPublisher ? ":" : ".");
        }
        if (hasPublisher) {
            writer.write(" ");
            writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(source.publisher()));
            writer.write(".");
        }
        if (ModuleGuideLaTeXWriter.isSet(source.isbn())) {
            writer.write(" ISBN: ");
            writer.write(source.isbn());
            writer.write(".");
        }
        if (ModuleGuideLaTeXWriter.isSet(source.doi())) {
            writer.write(" DOI: ");
            writer.write(source.doi());
            writer.write(".");
        }
        if (ModuleGuideLaTeXWriter.isSet(source.url())) {
            writer.write(" URL: \\url{");
            writer.write(source.url());
            writer.write("}.");
        }
        Main.newLine(writer);
        writer.write("\\end{minipage}");
    }

    private static void writeStats(
        final ModuleStats stats,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        ModuleGuideLaTeXWriter.writeStats(stats, Optional.empty(), internationalization, writer);
    }

    private static void writeStats(
        final ModuleStats stats,
        final Optional<String> elective,
        final Internationalization internationalization,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\begin{minipage}{");
        writer.write(
            elective.isEmpty() ?
                ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE :
                    ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE_ELECTIVE
        );
        writer.write("}\\raggedright\\strut{}\\hyperref[sec:");
        writer.write(stats.id());
        writer.write("]{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(stats.title()));
        writer.write("}\\strut{}\\end{minipage} & ");
        if (elective.isPresent()) {
            writer.write(elective.get());
            writer.write(" & ");
        }
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
        writer.write(ModuleGuideLaTeXWriter.formatExamination(stats.examination(), internationalization));
        writer.write("\\\\\\hline");
        Main.newLine(writer);
    }

    public ModuleGuideLaTeXWriter(final ModuleGuide guide) {
        super(guide);
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
    protected void writeIntro(final BufferedWriter writer) throws IOException {
        writer.write("\\pagestyle{fancy}");
        Main.newLine(writer);
        Main.newLine(writer);
        final Internationalization internationalization = this.guide.generalLanguage().getInternationalization();
        writer.write(internationalization.internationalize(InternationalizationKey.GREETING_STUDENTS));
        writer.write(",\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.GREETING_PARTNERS));
        writer.write(",\\\\");
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.GREETING_COLLEAGUES));
        writer.write(",\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(
            internationalization.introduction(
                this.guide.degree().substring(0, this.guide.degree().indexOf(' ')),
                ModuleGuideLaTeXWriter.escapeForLaTeX(this.guide.subject()),
                this.guide.year()
            )
        );
        writer.write("\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.INTRO));
        writer.write("\\\\[2ex]");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write(internationalization.internationalize(InternationalizationKey.REGARDS));
        Main.newLine(writer);
        Main.newLine(writer);
        switch (this.guide.signature()) {
        case GREGOR:
            writer.write("\\includegraphics{signature.png}");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("Prof. Dr. Gregor Sandhaus\\\\");
            Main.newLine(writer);
            writer.write(internationalization.internationalize(InternationalizationKey.DEAN_CS));
            break;
        case ANGELIKA:
            writer.write("Prof. Dr. ANGELIKA RÖCHTER\\\\");
            Main.newLine(writer);
            writer.write(internationalization.internationalize(InternationalizationKey.DEAN_BA));
            break;
        }
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
        final int weightSum,
        final String modulesFolder,
        final BufferedWriter writer
    ) throws IOException {
        final Internationalization internationalization = this.guide.generalLanguage().getInternationalization();
        final List<String> linkable = this.guide.modules().stream().map(module -> module.meta().module()).toList();
        String specialization = "";
        int semester = 0;
        for (final Module module : this.guide.modules().stream().sorted().toList()) {
            if (module.meta().specialization() != null && !specialization.equals(module.meta().specialization())) {
                specialization = module.meta().specialization();
                if (Main.ELECTIVE.equals(specialization)) {
                    writer.write("\\chapter{");
                    writer.write(internationalization.internationalize(InternationalizationKey.ELECTIVE_MODULES));
                    writer.write("}");
                } else {
                    writer.write("\\chapter{");
                    writer.write(internationalization.internationalize(InternationalizationKey.SPECIALIZATION));
                    writer.write(" ");
                    writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(specialization));
                    writer.write("}");
                }
                Main.newLine(writer);
                Main.newLine(writer);
            } else if (
                (module.meta().specialization() == null || module.meta().specialization().isBlank())
                && module.meta().semester() != semester
            ) {
                semester = module.meta().semester();
                writer.write("\\chapter{");
                writer.write(internationalization.enumerate(semester));
                writer.write(" ");
                writer.write(internationalization.internationalize(InternationalizationKey.SEMESTER));
                writer.write("}");
                Main.newLine(writer);
                Main.newLine(writer);
            }
            ModuleGuideLaTeXWriter.writeModule(module, weightSum, linkable, modulesFolder, writer);
        }
    }

    @Override
    protected void writeOverview(
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException {
        final Internationalization internationalization = this.guide.generalLanguage().getInternationalization();
        writer.write("\\tableofcontents");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeExaminationTypes(internationalization, writer);
        writer.write("\\chapter{");
        writer.write(internationalization.internationalize(InternationalizationKey.STUDY_PLAN));
        writer.write(" -- ");
        writer.write(internationalization.internationalize(InternationalizationKey.TEACHING_EVENTS));
        writer.write("}\\label{chap:studyplan}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{tikzpicture}");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (subject) {\\large\\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(this.guide.subject().toUpperCase()));
        writer.write("}};");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (degree) [below=0.5 of subject.west, anchor=west] {\\large ");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(this.guide.degree()));
        writer.write("};");
        Main.newLine(writer);
        writer.write("\\node[fhdwblue] (time) [below=0.5 of degree.west, anchor=west] {\\large ");
        writer.write(internationalization.internationalize(this.guide.timeModel().internationalizationKey));
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
        Main.newLine(writer);
        writer.write("\\renewcommand{\\llb}{}");
        Main.newLine(writer);
        Main.newLine(writer);
        ModuleGuideLaTeXWriter.writeLongtableHeader(true, internationalization, writer);
        int semester = 1;
        int groupsOnPage = 0;
        int pagebreakIndex = 0;
        int numberOfSpecializationModules = 0;
        int numberOfElectiveModules = 0;
        final int[] pagebreaks = ModuleGuideLaTeXWriter.toPagebreaks(this.guide.pagebreaks());
        final String seeElective =
            internationalization.internationalize(InternationalizationKey.SEE_ELECTIVE);
        final String seeSpecialization =
            internationalization.internationalize(InternationalizationKey.SEE_SPECIALIZATION);
        for (final List<ModuleStats> modules : overview.semesters()) {
            if (pagebreakIndex < pagebreaks.length && groupsOnPage >= pagebreaks[pagebreakIndex]) {
                ModuleGuideLaTeXWriter.writePagebreakForLongtable(internationalization, writer);
                groupsOnPage = 0;
                pagebreakIndex++;
            }
            writer.write("\\rowcolor{fhdwblue}\\multicolumn{6}{c}{\\textcolor{white}{");
            writer.write(internationalization.enumerate(semester));
            writer.write(" ");
            writer.write(internationalization.internationalize(InternationalizationKey.SEMESTER));
            writer.write("}}\\\\\\hline");
            Main.newLine(writer);
            for (final ModuleStats stats : modules) {
                ModuleGuideLaTeXWriter.writeStats(stats, internationalization, writer);
                if (seeSpecialization.equals(stats.examination())) {
                    numberOfSpecializationModules++;
                }
                if (seeElective.equals(stats.examination())) {
                    numberOfElectiveModules++;
                }
            }
            semester++;
            groupsOnPage++;
        }
        writer.write("\\rowcolor{fhdwblue}\\begin{minipage}{");
        writer.write(ModuleGuideLaTeXWriter.OVERVIEW_FIRST_COL_SIZE);
        writer.write("}\\textcolor{white}{");
        writer.write(internationalization.internationalize(InternationalizationKey.SUM));
        writer.write("}\\end{minipage} &  & \\textcolor{white}{");
        writer.write(String.valueOf(overview.contactHoursSum()));
        writer.write("} & \\textcolor{white}{");
        writer.write(String.valueOf(overview.homeHoursSum()));
        writer.write("} & \\textcolor{white}{");
        writer.write(String.valueOf(overview.ectsSum()));
        writer.write("} & \\\\\\hline");
        Main.newLine(writer);
        if (numberOfSpecializationModules > 0) {
            writer.write("\\end{longtable}");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\clearpage");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\chapter{");
            writer.write(internationalization.internationalize(InternationalizationKey.SPECIALIZATION_AREAS_HEADER));
            writer.write(" ");
            writer.write(ModuleStats.toRomanNumeral(numberOfSpecializationModules));
            writer.write("}\\label{chap:specialareas}");
            Main.newLine(writer);
            Main.newLine(writer);
            pagebreakIndex = 0;
            groupsOnPage = 0;
            final int[] pagebreaksSpecialization =
                ModuleGuideLaTeXWriter.toPagebreaks(this.guide.pagebreaksSpecialization());
            ModuleGuideLaTeXWriter.writeLongtableHeader(true, internationalization, writer);
            for (final Map.Entry<String, List<ModuleStats>> entry : overview.specializations().entrySet()) {
                if (Main.ELECTIVE.equals(entry.getKey())) {
                    continue;
                }
                if (
                    pagebreakIndex < pagebreaksSpecialization.length
                    && groupsOnPage >= pagebreaksSpecialization[pagebreakIndex]
                ) {
                    ModuleGuideLaTeXWriter.writePagebreakForLongtable(internationalization, writer);
                    groupsOnPage = 0;
                    pagebreakIndex++;
                }
                writer.write("\\rowcolor{fhdwblue}\\multicolumn{6}{c}{\\textcolor{white}{");
                writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(entry.getKey()));
                writer.write("}}\\\\\\hline");
                Main.newLine(writer);
                for (final ModuleStats stats : entry.getValue()) {
                    ModuleGuideLaTeXWriter.writeStats(stats, internationalization, writer);
                }
                groupsOnPage++;
            }
        }
        if (numberOfElectiveModules > 0) {
            writer.write("\\end{longtable}");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\clearpage");
            Main.newLine(writer);
            Main.newLine(writer);
            writer.write("\\chapter{");
            writer.write(internationalization.electiveHeader(numberOfElectiveModules));
            writer.write("}\\label{chap:electiveareas}");
            Main.newLine(writer);
            Main.newLine(writer);
            ModuleGuideLaTeXWriter.writeLongtableHeader(true, true, internationalization, writer);
            for (final Map.Entry<String, List<ModuleStats>> entry : overview.specializations().entrySet()) {
                if (!Main.ELECTIVE.equals(entry.getKey())) {
                    continue;
                }
                final Map<String, List<ModuleStats>> electiveByNumbers = new TreeMap<String, List<ModuleStats>>();
                for (final ModuleStats stats : entry.getValue()) {
                    final String numbers = ModuleGuideLaTeXWriter.computeNumbersForElectiveStats(stats, this.guide);
                    if (!electiveByNumbers.containsKey(numbers)) {
                        electiveByNumbers.put(numbers, new LinkedList<ModuleStats>());
                    }
                    final List<ModuleStats> statsList = electiveByNumbers.get(numbers);
                    if (statsList.stream().noneMatch(s -> s.id().equals(stats.id()))) {
                        statsList.add(stats);
                    }
                }
                writer.write("\\rowcolor{fhdwblue}\\multicolumn{7}{c}{\\textcolor{white}{");
                writer.write(internationalization.internationalize(InternationalizationKey.ELECTIVE_MODULES));
                writer.write("}}\\\\\\hline");
                Main.newLine(writer);
                for (final Map.Entry<String, List<ModuleStats>> electiveEntry : electiveByNumbers.entrySet()) {
                    for (final ModuleStats stats : electiveEntry.getValue()) {
                        ModuleGuideLaTeXWriter.writeStats(
                            stats,
                            Optional.of(electiveEntry.getKey()),
                            internationalization,
                            writer
                        );
                    }
                }
            }
        }
        writer.write("\\end{longtable}");
        Main.newLine(writer);
        writer.write("\\renewcommand{\\arraystretch}{1}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\renewcommand{\\llb}{\\\\}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\clearpage");
        Main.newLine(writer);
        Main.newLine(writer);
    }

    @Override
    protected void writeTitlePage(final BufferedWriter writer) throws IOException {
        final Internationalization internationalization = this.guide.generalLanguage().getInternationalization();
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
        writer.write("\\Large ");
        writer.write(internationalization.internationalize(InternationalizationKey.MODULE_GUIDE));
        writer.write(" ");
        writer.write(internationalization.internationalize(this.guide.timeModel().internationalizationKey));
        writer.write("\\\\[1ex]");
        Main.newLine(writer);
        writer.write("\\Huge \\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(this.guide.subject()));
        writer.write("}\\\\[0.5ex]");
        Main.newLine(writer);
        writer.write("\\Huge \\textbf{");
        writer.write(ModuleGuideLaTeXWriter.escapeForLaTeX(this.guide.degree()));
        writer.write("}\\\\[0.5ex]");
        Main.newLine(writer);
        writer.write("\\Large ");
        writer.write(internationalization.internationalize(InternationalizationKey.STUDY_YEAR));
        writer.write(" ");
        writer.write(this.guide.year());
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
