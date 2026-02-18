package moduleguidehelper;

public class German implements Internationalization {

    @Override
    public String electiveHeader(final int maxNumber) {
        if (maxNumber > 1) {
            return "Wahlpflichtmodule I bis " + ModuleStats.toRomanNumeral(maxNumber);
        }
        return "Wahlpflichtmodule";
    }

    @Override
    public String enumerate(final int number) {
        return String.format("%d.", number);
    }

    @Override
    public String internationalize(final InternationalizationKey key) {
        switch (key) {
        case CONTACT_HOURS:
            return "Kontaktstunden";
        case CONTACT_HOURS_HEADER:
            return "KONTAKT\\-STUNDEN";
        case CONTENTS:
            return "Inhalte";
        case DEAN_BA:
            return "Dekanin Betriebswirtschaft";
        case DEAN_CS:
            return "Dekan des Fachbereichs Informatik";
        case DEFAULT_TEACHING:
            return "Präsenzveranstaltungen, Eigenstudium, individuelles und kooperatives Lernen, "
                + "problemorientiertes und integratives Lernen, forschendes Lernen, synchrones und "
                + "asynchrones Lernen, Übungen, Fallstudien, Expertenvorträge, Projekte, Gruppenarbeit";
        case DURATION:
            return "Dauer";
        case ECTS:
            return "ECTS-Punkte";
        case ECTS_HEADER:
            return "CREDIT POINTS (ECTS)";
        case EDITION:
            return "Auflage";
        case ELECTIVE_HEADER:
            return "BLOCK";
        case ELECTIVE_MODULE_PATTERN:
            return "Wahlpflichtmodul %s";
        case ELECTIVE_MODULES:
            return "Wahlpflichtmodule";
        case EXAM_FORM:
            return "Die Prüfung besteht aus einer 90-minütigen Klausur.";
        case EXAMINATION:
            return "Prüfungsleistung";
        case EXAMINATION_HEADER:
            return "PRÜ\\-FUNGS\\-FORM";
        case EXAMINATIONS:
            return "Prüfungsleistungen";
        case EXAMINATIONS_ANNOUNCEMENT:
            return "Die jeweils ausgewählte Prüfungsform wird zu Beginn der entsprechenden Veranstaltung "
                + "bekanntgegeben.";
        case EXAMINATIONS_HEADER1:
            return "PRÜFUNG";
        case EXAMINATIONS_HEADER2:
            return "LEISTUNG";
        case EXAMINATIONS_INTRO:
            return "Zu Anlage 1 gehört die folgende Legende, welche Art und Umfang der Prüfungsleistungen näher "
                + "erläutert";
        case EXAMINATIONS_MULTIPLE:
            return "Sind mehrere Prüfungsformen angegeben, stellt dies eine Auswahl aus den angegeben Alternativen "
                + "dar; nicht jedoch die Kombination dieser verschiedenen Prüfungsformen.";
        case EXAMINATIONS_PREFERRED:
            return "Ist bei mehreren Prüfungsformen eine davon hervorgehoben, so wird diese bevorzugt.";
        case FREQUENCY:
            return "Häufigkeit";
        case FULLTIME:
            return "Vollzeit";
        case GENERAL_INFORMATION:
            return "Allgemeine Angaben";
        case GREETING_COLLEAGUES:
            return "sehr geehrte Kolleginnen und Kollegen";
        case GREETING_PARTNERS:
            return "sehr geehrte Kooperationspartner";
        case GREETING_STUDENTS:
            return "Sehr geehrte Studierende";
        case ID:
            return "Kürzel";
        case INTRO:
            return "Dieses Modulhandbuch stellt zum einen für die Studierenden eine Information über die "
                + "Studieninhalte dar, zum Zweiten dient es den Partnerunternehmen als Hilfe zur inhaltlichen "
                + "Vorbereitung der Praxisphasen. Daneben ist diese Übersicht ein Leitfaden für die "
                + "Dozentinnen und Dozenten zur modulübergreifenden Abstimmung der Lehrinhalte.";
        case KEYWORDS:
            return "Stichwörter";
        case MODULE_GUIDE:
            return "Modulhandbuch";
        case MODULE_HEADER:
            return "MODUL";
        case NONE:
            return "Keine";
        case NUMBER:
            return "Nummer";
        case PAGE:
            return "Seite";
        case PAGE_ABR_PLURAL:
            return "S";
        case PAGE_ABR_SINGULAR:
            return "S";
        case PAPER_FORM:
            return "Die Prüfung besteht aus einer Studienarbeit.";
        case PARTTIME:
            return "Teilzeit";
        case PORTFOLIO_FORM:
            return "Die Prüfung ist eine kombinierte Prüfung aus \\textbf{entweder} einer Klausur und einem "
                + "Referat \\textbf{oder} aus zwei Klausuren; der kombinierte Prüfungsumfang muss dabei dem einer "
                + "Einzelprüfung als Klausur oder Referat entsprechen (bei zwei Klausuren addieren sich "
                + "beispielsweise die Bearbeitungszeiten dieser beiden Klausuren zu 90 Minuten auf).";
        case PRACTICAL_FORM:
            return "Die Prüfung ist eine praktische Prüfung.";
        case PRESENTATION_FORM:
            return "Die Prüfung besteht aus einem Referat.";
        case QUALIFICATION:
            return "Qualifikations- und Kompetenzziele";
        case QUALIFICATION_START:
            return "Nach erfolgreichem Abschluss dieses Moduls sind die Studierenden in der Lage";
        case RECOMMENDATIONS:
            return "Zugangsempfehlungen";
        case RECOMMENDED_LITERATURE:
            return "Ergänzende Literaturempfehlungen";
        case REGARDS:
            return "Mit freundlichen Grüßen";
        case REQUIRED_LITERATURE:
            return "Grundlegende Literaturhinweise";
        case REQUIREMENTS:
            return "Zugangsvoraussetzungen";
        case RESPONSIBLE:
            return "Modulverantwortung";
        case SEE_ELECTIVE:
            return "S. Wahlpflicht";
        case SEE_SPECIALIZATION:
            return "S. Spezialisierung";
        case SELF_STUDY:
            return "Selbststudium";
        case SELF_STUDY_HEADER:
            return "SELBST\\-STUDIUM";
        case SEMESTER:
            return "Semester";
        case SEMESTER_HEADER:
            return "SEMES\\-TER";
        case SPECIALIZATION:
            return "Spezialisierung";
        case SPECIALIZATION_AREAS_HEADER:
            return "Spezialisierungsbereiche mit jeweils den Modulen I bis";
        case SPECIALIZATION_MODULE_PATTERN:
            return "Modul %s aus Spezialisierung";
        case STUDY_PLAN:
            return "Studienplan";
        case STUDY_YEAR:
            return "Studienjahr";
        case SUM:
            return "Summe";
        case TEACHING_LANGUAGE:
            return "Lehrsprache";
        case TEACHING_EVENTS:
            return "Lehrveranstaltungen";
        case TEACHING_METHODS:
            return "Lehr- und Lernmethoden";
        case VOLUME:
            return "Ausgabe";
        default:
            return null;
        }
    }

    @Override
    public String introduction(final String degreeType, final String subject, final String year) {
        return String.format("Sie erhalten das Modulhandbuch für den %s-Studiengang %s im Studienjahr %s.", degreeType, subject, year);
    }

}
