package com.travelsync.model;

import java.util.*;

public class TravelPreference {
    // 8 distinct feature dimensions representing travel styles (Bit 0 to Bit 7)
    public static final int BEACH        = 1 << 0; // 1
    public static final int HISTORICAL   = 1 << 1; // 2
    public static final int ADVENTURE    = 1 << 2; // 4
    public static final int NATURE       = 1 << 3; // 8
    public static final int FOOD         = 1 << 4; // 16
    public static final int NIGHTLIFE    = 1 << 5; // 32
    public static final int RELAXATION   = 1 << 6; // 64
    public static final int SHOPPING     = 1 << 7; // 128

    public static final int TOTAL_BITS = 8;
    public static final int MAX_MASK = (1 << TOTAL_BITS); // 256

    private static final Map<String, Integer> NAME_TO_BIT = new LinkedHashMap<>();
    private static final Map<Integer, String> BIT_TO_NAME = new LinkedHashMap<>();

    static {
        register("beach", BEACH);
        register("historical", HISTORICAL);
        register("adventure", ADVENTURE);
        register("nature", NATURE);
        register("food", FOOD);
        register("nightlife", NIGHTLIFE);
        register("relaxation", RELAXATION);
        register("shopping", SHOPPING);
    }

    private static void register(String name, int bit) {
        NAME_TO_BIT.put(name.toLowerCase(), bit);
        BIT_TO_NAME.put(bit, name);
    }

    public static int toMask(Collection<String> preferences) {
        if (preferences == null) return 0;
        int mask = 0;
        for (String pref : preferences) {
            Integer bit = NAME_TO_BIT.get(pref.toLowerCase().trim());
            if (bit != null) {
                mask |= bit;
            }
        }
        return mask;
    }

    public static List<String> fromMask(int mask) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : BIT_TO_NAME.entrySet()) {
            if ((mask & entry.getKey()) != 0) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public static Map<String, Integer> getAllPreferences() {
        return Collections.unmodifiableMap(NAME_TO_BIT);
    }
}
