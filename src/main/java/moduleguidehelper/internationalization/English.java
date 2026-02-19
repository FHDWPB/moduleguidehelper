package moduleguidehelper.internationalization;

import moduleguidehelper.model.*;

public class English implements Internationalization {

    @Override
    public String electiveHeader(final int maxNumber) {
        if (maxNumber > 1) {
            return "Elective modules I to " + ModuleStats.toRomanNumeral(maxNumber);
        }
        return "Elective modules";
    }

    @Override
    public String enumerate(final int number) {
        if (number % 100 == 11 || number % 100 == 12) {
            return String.format("%dth", number);
        }
        if (number % 10 == 1) {
            return String.format("%dst", number);
        }
        if (number % 10 == 2) {
            return String.format("%dnd", number);
        }
        if (number % 10 == 3) {
            return String.format("%drd", number);
        }
        return String.format("%dth", number);
    }

    @Override
    public String internationalize(final InternationalizationKey key) {
        switch (key) {
        case CONTACT_HOURS:
            return "Contact hours";
        case CONTACT_HOURS_HEADER:
            return "CONTACT HOURS";
        case CONTENTS:
            return "Contents";
        case DEAN_BA:
            return "Dean of the faculty Business Administration";
        case DEAN_CS:
            return "Dean of the faculty Computer Science";
        case DEFAULT_TEACHING:
            return "Präsenzveranstaltungen, Eigenstudium, individuelles und kooperatives Lernen, "
                + "problemorientiertes und integratives Lernen, forschendes Lernen, synchrones und "
                + "asynchrones Lernen, Übungen, Fallstudien, Expertenvorträge, Projekte, Gruppenarbeit";
        case DURATION:
            return "Duration";
        case ECTS:
            return "Credit points (ECTS)";
        case ECTS_HEADER:
            return "CREDIT POINTS (ECTS)";
        case EDITION:
            return "edition";
        case ELECTIVE_HEADER:
            return "BLOCK";
        case ELECTIVE_MODULE_PATTERN:
            return "Elective module %s";
        case ELECTIVE_MODULES:
            return "Elective modules";
        case EXAM_FORM:
            return "The examination is a written exam with a duration of 90 minutes.";
        case EXAMINATION:
            return "Examination";
        case EXAMINATION_HEADER:
            return "EXA\\-MINA\\-TION";
        case EXAMINATIONS:
            return "Examination forms";
        case EXAMINATIONS_ANNOUNCEMENT:
            return "The chosen examination form is announced at the beginning of each module.";
        case EXAMINATIONS_HEADER1:
            return "EXAMINATION";
        case EXAMINATIONS_HEADER2:
            return "FORM";
        case EXAMINATIONS_INTRO:
            return "";
        case EXAMINATIONS_MULTIPLE:
            return "If multiple examination forms are listed, this means a choice between these alternative forms; "
                + "not their combination.";
        case EXAMINATIONS_PREFERRED:
            return "If one examination form is emphasized, it is the preferred form.";
        case FREQUENCY:
            return "Frequency";
        case FULLTIME:
            return "Fulltime";
        case GENERAL_INFORMATION:
            return "General Information";
        case GREETING_COLLEAGUES:
            return "";
        case GREETING_PARTNERS:
            return "";
        case GREETING_STUDENTS:
            return "";
        case ID:
            return "ID";
        case INTRO:
            return "";
        case KEYWORDS:
            return "Keywords";
        case MODULE_GUIDE:
            return "Module Guide";
        case MODULE_HEADER:
            return "MODULE";
        case NONE:
            return "none";
        case NUMBER:
            return "number";
        case PAGE:
            return "Page";
        case PAGE_ABR_PLURAL:
            return "pp";
        case PAGE_ABR_SINGULAR:
            return "p";
        case PAPER_FORM:
            return "The examination is a scientific paper.";
        case PARTTIME:
            return "Parttime";
        case PORTFOLIO_FORM:
            return "The examination is a combined form of \\textbf{either} one written exam and one presentation "
                + "\\textbf{or} two written exams; the combined effort for the examination must match that of a "
                + "single examination form (for instance, the durations of two exams must add up to 90 minutes).";
        case PRACTICAL_FORM:
            return "The examination is a practical examination.";
        case PRESENTATION_FORM:
            return "The examination is a presentation.";
        case QUALIFICATION:
            return "Qualification and Competency Goals";
        case QUALIFICATION_START:
            return "After successful completion of the module, students are able to";
        case RECOMMENDATIONS:
            return "Recommended Prerequisites";
        case RECOMMENDED_LITERATURE:
            return "Recommended Additional Literature";
        case REGARDS:
            return "";
        case REQUIRED_LITERATURE:
            return "Required Literature";
        case REQUIREMENTS:
            return "Required Prerequisites";
        case RESPONSIBLE:
            return "Responsible";
        case SEE_ELECTIVE:
            return "see elective modules";
        case SEE_SPECIALIZATION:
            return "see specializations";
        case SELF_STUDY:
            return "Self-study";
        case SELF_STUDY_HEADER:
            return "SELF-STUDY";
        case SEMESTER:
            return "semester";
        case SEMESTER_HEADER:
            return "SEMES\\-TER";
        case SPECIALIZATION:
            return "Specialization";
        case SPECIALIZATION_AREAS_HEADER:
            return "Specialization areas with corresponding modules I to";
        case SPECIALIZATION_MODULE_PATTERN:
            return "Specialization module %s";
        case STUDY_PLAN:
            return "study plan";
        case STUDY_YEAR:
            return "year of study";
        case SUM:
            return "Sum";
        case TEACHING_LANGUAGE:
            return "Teaching Language";
        case TEACHING_EVENTS:
            return "Teaching Events";
        case TEACHING_METHODS:
            return "Teaching and Learning Methods";
        case VOLUME:
            return "volume";
        default:
            return null;
        }
    }

    @Override
    public String introduction(final String degreeType, final String subject, final String year) {
        // TODO Auto-generated method stub
        return null;
    }

}
