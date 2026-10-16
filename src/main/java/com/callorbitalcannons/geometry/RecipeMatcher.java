package com.callorbitalcannons.geometry;

/**
 * Pure 3x3 crafting-grid matcher for the two weapon-rod recipes.
 *
 * <p>Slots are row-major (0..8). Each slot is described by an item id:
 * {@code "empty"}, {@code "tnt"}, {@code "rod"}, or anything else
 * ({@code "other"}), plus the stack count in that slot.
 *
 * <p>Orbital pattern: full TNT stack (64) in the center of the top row
 * (slot 1), an ordinary fishing rod in the center (slot 4), everything else
 * empty. Stab pattern: rod in the center (slot 4), full TNT stack (64) in
 * the center of the bottom row (slot 7), everything else empty. The two
 * patterns are intentionally not mirror images of each other and must never
 * cross-match.
 */
public final class RecipeMatcher {
    private RecipeMatcher() {
    }

    public enum Match {
        NONE,
        ORBITAL,
        STAB
    }

    /** Minimum TNT count accepted as "one full stack". */
    public static final int REQUIRED_TNT = 64;

    public static Match match(String[] ids, int[] counts) {
        if (ids == null || counts == null || ids.length != 9 || counts.length != 9) {
            return Match.NONE;
        }
        boolean orbitalShape = isTntStack(ids, counts, 1) && isRod(ids, 4)
                && emptyExcept(ids, 1, 4);
        boolean stabShape = isRod(ids, 4) && isTntStack(ids, counts, 7)
                && emptyExcept(ids, 4, 7);
        if (orbitalShape && !stabShape) {
            return Match.ORBITAL;
        }
        if (stabShape && !orbitalShape) {
            return Match.STAB;
        }
        return Match.NONE;
    }

    private static boolean isTntStack(String[] ids, int[] counts, int slot) {
        return "tnt".equals(ids[slot]) && counts[slot] >= REQUIRED_TNT;
    }

    private static boolean isRod(String[] ids, int slot) {
        return "rod".equals(ids[slot]);
    }

    private static boolean emptyExcept(String[] ids, int keepA, int keepB) {
        for (int i = 0; i < 9; i++) {
            if (i != keepA && i != keepB && !"empty".equals(ids[i])) {
                return false;
            }
        }
        return true;
    }
}
