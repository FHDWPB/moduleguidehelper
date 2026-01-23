package modulebookhelper;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ModuleBookLaTeXWriter extends ModuleBookWriter {

    private static String escapeForLaTeX(final String text) {
        return text.replaceAll("\\\\", "\\\\textbackslash")
            .replaceAll("([&\\$%\\{\\}_#])", "\\\\$1")
            .replaceAll("~", "\\\\textasciitilde{}")
            .replaceAll("\\^", "\\\\textasciicircum{}")
            .replaceAll("\\\\textbackslash", "\\\\textbackslash{}")
            .replaceAll("([^\\\\])\"", "$1''")
            .replaceAll("^\"", "''");
    }

    private static void writeModule(
        final MetaModule meta,
        final Module module,
        final int weightSum,
        final BufferedWriter writer
    ) throws IOException {
        final String[][] table = new String[13][2];
        table[0][0] = "Kürzel";
        table[0][1] = meta.module();
        table[1][0] = "Modulverantwortliche";
        table[1][1] = module.responsible();
        table[2][0] = "Dozenten";
        table[2][1] = module.teachers().stream().collect(Collectors.joining(", "));
        table[3][0] = "Lehrsprache";
        table[3][1] = module.language();
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
        table[9][1] = meta.type();
        table[10][0] = "Häufigkeit";
        table[10][1] = meta.frequency();
        table[11][0] = "Gewichtung";
        table[11][1] = String.format("%d/%d", meta.weight(), weightSum);
        table[12][0] = "Prüfungsleistung";
        table[12][1] = module.examination();
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
            writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(table[i][0]));
            writer.write("} & ");
            writer.write(ModuleBookLaTeXWriter.escapeForLaTeX(table[i][1]));
            writer.write("\\\\\\arrayrulecolor{mtabgray}\\hline");
            Main.newLine(writer);
        }
        writer.write("\\end{tabularx}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Stichwörter}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\begin{itemize}");
        Main.newLine(writer);
        for (final String keyword : module.keywords()) {
            writer.write("\\item ");
            writer.write(keyword);
            Main.newLine(writer);
        }
        writer.write("\\end{itemize}");
        Main.newLine(writer);
        Main.newLine(writer);
        writer.write("\\subsection*{Zugangsvoraussetzungen}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\subsection*{Verwendbarkeit}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\subsection*{Qualifikations- und Kompetenzziele}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\subsection*{Lehr- und Lernmethoden}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\subsection*{Inhalte}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\subsection*{Grundlegende Literaturhinweise}");
        Main.newLine(writer);
        Main.newLine(writer);
        //TODO
        writer.write("\\clearpage");
        Main.newLine(writer);
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
            ModuleBookLaTeXWriter.writeModule(meta, modules.get(meta.module()), weightSum, writer);
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
                writer.write(stats.examination());
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
    protected void writerIntro(final ModuleBook book, final BufferedWriter writer) throws IOException {
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
