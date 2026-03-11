package moduleguidehelper.model;

import java.util.*;

import moduleguidehelper.*;

public record Module(MetaModule meta, RawModule module) {

    public static Comparator<Module> createComparator(final List<String> specializationOrder) {
        return new Comparator<Module>() {

            @Override
            public int compare(final Module module1, final Module module2) {
                final MetaModule meta1 = module1.meta();
                final MetaModule meta2 = module2.meta();
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
                        if (specializationOrder != null && !specializationOrder.isEmpty()) {
                            final int index1 = specializationOrder.indexOf(meta1.specialization());
                            final int index2 = specializationOrder.indexOf(meta2.specialization());
                            if (index1 != index2) {
                                return Integer.compare(index1, index2);
                            }
                        }
                        final int compare = meta1.specialization().compareTo(meta2.specialization());
                        if (compare != 0) {
                            return compare;
                        }
                        return meta1.specializationnumber().compareTo(meta2.specializationnumber());
                    }
                    return 1;
                }
                if (meta2.specializationnumber() != null) {
                    return -1;
                }
                final int compare = meta1.semester() - meta2.semester();
                if (compare != 0) {
                    return compare;
                }
                if (meta1.sempos() != null) {
                    if (meta2.sempos() != null) {
                        return meta1.sempos().compareTo(meta2.sempos());
                    }
                    return -1;
                }
                if (meta2.sempos() != null) {
                    return 1;
                }
                return 0;
            }

        };
    }

}
