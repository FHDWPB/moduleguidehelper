package moduleguidehelper;

import java.math.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.fraction.*;

public record ModuleOverview(
    List<List<ModuleStats>> semesters,
    Map<String, List<ModuleStats>> specializations,
    int contactHoursSum,
    int homeHoursSum,
    int ectsSum
) {

    public static ModuleOverview create(final ModuleGuide book, final ModuleMap modules) {
        final List<List<ModuleStats>> semesters = new ArrayList<List<ModuleStats>>();
        final Map<Integer, List<ModuleStats>> semesterMap = new TreeMap<Integer, List<ModuleStats>>();
        final Map<Integer, ModuleStats> specializationModulesMap = new LinkedHashMap<Integer, ModuleStats>();
        final Map<String, List<ModuleStats>> specializations = new LinkedHashMap<String, List<ModuleStats>>();
        int ectsSum = 0;
        int contactHoursSum = 0;
        int homeHoursSum = 0;
        for (final MetaModule meta : book.modules().stream().sorted().toList()) {
            final Module module = modules.get(meta.module());
            if (module == null) {
                System.out.println(meta.module());
                continue;
            }
            final BigFraction contactHoursFactor = ModuleOverview.parseFactor(meta.contacthoursfactor());
            final BigFraction homeHoursFactor = ModuleOverview.parseFactor(meta.homehoursfactor());
            final BigFraction ectsFactor = ModuleOverview.parseFactor(meta.ectsfactor());
            final BigFraction contactHours = contactHoursFactor.multiply(module.contacthours());
            final BigFraction homeHours = homeHoursFactor.multiply(module.homehours());
            final BigFraction ects = ectsFactor.multiply(module.ects());
            if (
                !ModuleOverview.isInt(contactHours) || !ModuleOverview.isInt(homeHours) || !ModuleOverview.isInt(ects)
            ) {
                throw new IllegalArgumentException("Factors do not lead to integer result!");
            }
            final ModuleStats stats =
                new ModuleStats(
                    meta.module(),
                    module.title(),
                    meta.semester(),
                    meta.duration(),
                    contactHours.intValue(),
                    homeHours.intValue(),
                    ects.intValue(),
                    module.examination()
                );
            if (meta.specialization() != null) {
                final ModuleStats statsForSemester = stats.forSpecialization(meta.specializationNumber());
                if (specializationModulesMap.containsKey(meta.specializationNumber())) {
                    final ModuleStats check = specializationModulesMap.get(meta.specializationNumber());
                    if (
                        statsForSemester.contactHours() != check.contactHours()
                        || statsForSemester.homeHours() != check.homeHours()
                        || statsForSemester.ects() != check.ects()
                    ) {
                        throw new IllegalArgumentException("Specialization stats do not match!");
                    }
                } else {
                    specializationModulesMap.put(meta.specializationNumber(), statsForSemester);
                    contactHoursSum += contactHours.intValue();
                    homeHoursSum += homeHours.intValue();
                    ectsSum += ects.intValue();
                    semesterMap.merge(meta.semester(), List.of(statsForSemester), ModuleOverview::concatLists);
                }
                specializations.merge(meta.specialization(), List.of(stats), ModuleOverview::concatLists);
            } else {
                contactHoursSum += contactHours.intValue();
                homeHoursSum += homeHours.intValue();
                ectsSum += ects.intValue();
                semesterMap.merge(meta.semester(), List.of(stats), ModuleOverview::concatLists);
            }
        }
        for (final Map.Entry<Integer, List<ModuleStats>> entry : semesterMap.entrySet()) {
            semesters.add(entry.getValue());
        }
        return new ModuleOverview(semesters, specializations, contactHoursSum, homeHoursSum, ectsSum);
    }

    private static <E> List<E> concatLists(final List<E> list1, final List<E> list2) {
        return Stream.of(list1, list2).flatMap(Collection::stream).toList();
    }

    private static boolean isInt(final BigFraction number) {
        return number.getDenominator().equals(BigInteger.ONE);
    }

    private static BigFraction parseFactor(final String factor) {
        return factor == null ? BigFraction.ONE : ModuleOverview.parseRationalNumber(factor);
    }

    private static BigFraction parseRationalNumber(final String number) {
        if (number.contains(".")) {
            final String[] parts = number.split("\\.", -1);
            if (parts.length != 2) {
                throw new NumberFormatException(String.format("Number %s contains more than one dot!", number));
            }
            final int exponent = parts[1].length();
            final int denominator = Integer.parseInt("1" + "0".repeat(exponent));
            final int beforeComma = parts[0].length() == 0 ? 0 : Integer.parseInt(parts[0]);
            final int afterComma = parts[1].length() == 0 ? 0 : Integer.parseInt(parts[1]);
            return new BigFraction(beforeComma * denominator + afterComma, denominator);
        }
        final String[] parts = number.split("/");
        if (parts.length > 2) {
            throw new NumberFormatException(String.format("Number %s contains more than one slash!", number));
        }
        return parts.length == 1 ?
            new BigFraction(Integer.parseInt(parts[0].strip())) :
                new BigFraction(Integer.parseInt(parts[0].strip()), Integer.parseInt(parts[1].strip()));
    }

}
