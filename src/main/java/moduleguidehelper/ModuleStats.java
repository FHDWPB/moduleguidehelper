package moduleguidehelper;

import java.util.*;

public record ModuleStats(
    String id,
    String title,
    int semester,
    int duration,
    int contactHours,
    int homeHours,
    int ects,
    String examination
) {

    private static final TreeMap<Integer, String> ROMAN_NUMERALS;

    static {
        ROMAN_NUMERALS = new TreeMap<Integer, String>();
        ModuleStats.ROMAN_NUMERALS.put(1000, "M");
        ModuleStats.ROMAN_NUMERALS.put(900, "CM");
        ModuleStats.ROMAN_NUMERALS.put(500, "D");
        ModuleStats.ROMAN_NUMERALS.put(400, "CD");
        ModuleStats.ROMAN_NUMERALS.put(100, "C");
        ModuleStats.ROMAN_NUMERALS.put(90, "XC");
        ModuleStats.ROMAN_NUMERALS.put(50, "L");
        ModuleStats.ROMAN_NUMERALS.put(40, "XL");
        ModuleStats.ROMAN_NUMERALS.put(10, "X");
        ModuleStats.ROMAN_NUMERALS.put(9, "IX");
        ModuleStats.ROMAN_NUMERALS.put(5, "V");
        ModuleStats.ROMAN_NUMERALS.put(4, "IV");
        ModuleStats.ROMAN_NUMERALS.put(1, "I");
    }

    public ModuleStats forSpecialization(final int number) {
        return new ModuleStats(
            this.id(),
            String.format("Fach %s aus Spezialisierung", ModuleStats.toRomanNumeral(number)),
            this.semester(),
            this.duration(),
            this.contactHours(),
            this.homeHours(),
            this.ects(),
            "S. Spezialisierung"
        );
    }

    private static String toRomanNumeral(final int number) {
        if (number < 1) {
            return "";
        }
        final int floorKey = ModuleStats.ROMAN_NUMERALS.floorKey(number);
        if (number == floorKey) {
            return ModuleStats.ROMAN_NUMERALS.get(number);
        }
        return ModuleStats.ROMAN_NUMERALS.get(floorKey) + ModuleStats.toRomanNumeral(number - floorKey);
    }

}
