package moduleguidehelper;

import java.math.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

import org.apache.commons.math3.fraction.*;

public record ModuleOverview(
    List<List<ModuleStats>> semesters,
    Map<String, List<ModuleStats>> specializations,
    int contactHoursSum,
    int homeHoursSum,
    int ectsSum,
    int weightSum
) {

    private static final Comparator<Module> OVERVIEW_COMPARATOR = new Comparator<Module>() {

        @Override
        public int compare(final Module module1, final Module module2) {
            final MetaModule meta1 = module1.meta();
            final MetaModule meta2 = module2.meta();
            final int compare = meta1.semester() - meta2.semester();
            if (compare != 0) {
                return compare;
            }
            if (meta1.sempos() != null) {
                if (meta2.sempos() != null) {
                    final int poscompare = meta1.sempos().compareTo(meta2.sempos());
                    if (poscompare != 0) {
                        return poscompare;
                    }
                    if (meta1.specializationnumber() != null) {
                        if (meta2.specializationnumber() != null) {
                            if (Main.ELECTIVE.equals(meta1.specialization())) {
                                if (Main.ELECTIVE.equals(meta2.specialization())) {
                                    return meta1.specializationnumber().compareTo(meta2.specializationnumber());
                                }
                                return 1;
                            }
                            if (Main.ELECTIVE.equals(meta2.specialization())) {
                                return -1;
                            }
                            final int speccompare = meta1.specialization().compareTo(meta2.specialization());
                            if (speccompare != 0) {
                                return speccompare;
                            }
                            return meta1.specializationnumber().compareTo(meta2.specializationnumber());
                        }
                        return 1;
                    }
                    if (meta2.specializationnumber() != null) {
                        return -1;
                    }
                }
                return -1;
            }
            if (meta2.sempos() != null) {
                return 1;
            }
            if (meta1.specializationnumber() != null) {
                if (meta2.specializationnumber() != null) {
                    if (Main.ELECTIVE.equals(meta1.specialization())) {
                        if (Main.ELECTIVE.equals(meta2.specialization())) {
                            return meta1.specializationnumber().compareTo(meta2.specializationnumber());
                        }
                        return 1;
                    }
                    if (Main.ELECTIVE.equals(meta2.specialization())) {
                        return -1;
                    }
                    final int speccompare = meta1.specialization().compareTo(meta2.specialization());
                    if (speccompare != 0) {
                        return speccompare;
                    }
                    return meta1.specializationnumber().compareTo(meta2.specializationnumber());
                }
                return 1;
            }
            if (meta2.specializationnumber() != null) {
                return -1;
            }
            return 0;
        }

    };

    public static ModuleOverview create(final ModuleGuide guide) {
        final Internationalization internationalization = guide.generalLanguage().getInternationalization();
        final List<List<ModuleStats>> semesters = new ArrayList<List<ModuleStats>>();
        final Map<Integer, List<ModuleStats>> semesterMap = new TreeMap<Integer, List<ModuleStats>>();
        final Map<Integer, ModuleStats> specializationModulesMap = new LinkedHashMap<Integer, ModuleStats>();
        final Map<Integer, ModuleStats> electiveModulesMap = new LinkedHashMap<Integer, ModuleStats>();
        final Map<String, List<ModuleStats>> specializations = new TreeMap<String, List<ModuleStats>>();
        int ectsSum = 0;
        int contactHoursSum = 0;
        int homeHoursSum = 0;
        int weightSum = 0;
        for (final Module module : guide.modules().stream().sorted(ModuleOverview.OVERVIEW_COMPARATOR).toList()) {
            final RawModule rawModule = module.module();
            final MetaModule meta = module.meta();
            final BigFraction contactHoursFactor = ModuleOverview.parseFactor(meta.contacthoursfactor());
            final BigFraction homeHoursFactor = ModuleOverview.parseFactor(meta.homehoursfactor());
            final BigFraction ectsFactor = ModuleOverview.parseFactor(meta.ectsfactor());
            final BigFraction contactHours = contactHoursFactor.multiply(rawModule.contacthours());
            final BigFraction homeHours = homeHoursFactor.multiply(rawModule.homehours());
            final BigFraction ects = ectsFactor.multiply(rawModule.ects());
            if (
                !ModuleOverview.isInt(contactHours) || !ModuleOverview.isInt(homeHours) || !ModuleOverview.isInt(ects)
            ) {
                throw new IllegalArgumentException("Factors do not lead to integer result!");
            }
            final ModuleStats stats =
                new ModuleStats(
                    meta.module(),
                    rawModule.title(),
                    meta.semester(),
                    meta.duration(),
                    contactHours.intValue(),
                    homeHours.intValue(),
                    ects.intValue(),
                    rawModule.examination()
                );
            if (meta.specialization() != null) {
                final boolean elective = Main.ELECTIVE.equals(meta.specialization());
                final ModuleStats statsForSemester =
                    stats.forSpecialization(meta.specializationnumber(), elective, internationalization);
                if (elective) {
                    if (electiveModulesMap.containsKey(meta.specializationnumber())) {
                        final ModuleStats check = electiveModulesMap.get(meta.specializationnumber());
                        if (
                            statsForSemester.contactHours() != check.contactHours()
                            || statsForSemester.homeHours() != check.homeHours()
                            || statsForSemester.ects() != check.ects()
                        ) {
                            throw new IllegalArgumentException("Elective stats do not match!");
                        }
                    } else {
                        electiveModulesMap.put(meta.specializationnumber(), statsForSemester);
                        contactHoursSum += contactHours.intValue();
                        homeHoursSum += homeHours.intValue();
                        ectsSum += ects.intValue();
                        weightSum += meta.weight();
                        semesterMap.merge(meta.semester(), List.of(statsForSemester), ModuleOverview::concatLists);
                    }
                } else {
                    if (specializationModulesMap.containsKey(meta.specializationnumber())) {
                        final ModuleStats check = specializationModulesMap.get(meta.specializationnumber());
                        if (
                            statsForSemester.contactHours() != check.contactHours()
                            || statsForSemester.homeHours() != check.homeHours()
                            || statsForSemester.ects() != check.ects()
                        ) {
                            throw new IllegalArgumentException("Specialization stats do not match!");
                        }
                    } else {
                        specializationModulesMap.put(meta.specializationnumber(), statsForSemester);
                        contactHoursSum += contactHours.intValue();
                        homeHoursSum += homeHours.intValue();
                        ectsSum += ects.intValue();
                        weightSum += meta.weight();
                        semesterMap.merge(meta.semester(), List.of(statsForSemester), ModuleOverview::concatLists);
                    }
                }
                specializations.merge(meta.specialization(), List.of(stats), ModuleOverview::concatLists);
            } else {
                contactHoursSum += contactHours.intValue();
                homeHoursSum += homeHours.intValue();
                ectsSum += ects.intValue();
                weightSum += meta.weight();
                semesterMap.merge(meta.semester(), List.of(stats), ModuleOverview::concatLists);
            }
        }
        for (final Map.Entry<Integer, List<ModuleStats>> entry : semesterMap.entrySet()) {
            semesters.add(entry.getValue());
        }
        if (weightSum != ectsSum) {
            Main.LOGGER.log(
                Level.SEVERE,
                String.format(
                    "WARNING: Sum of weights and ECTS does not match (%d vs. %d in %s)!",
                    weightSum,
                    ectsSum,
                    guide.title()
                )
            );
        }
        return new ModuleOverview(semesters, specializations, contactHoursSum, homeHoursSum, ectsSum, weightSum);
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
