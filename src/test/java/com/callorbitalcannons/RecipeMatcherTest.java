package com.callorbitalcannons;

import com.callorbitalcannons.geometry.RecipeMatcher;
import com.callorbitalcannons.geometry.RecipeMatcher.Match;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeMatcherTest {
    private static String[] grid(String... ids) {
        return ids;
    }

    private static int[] counts(int... counts) {
        return counts;
    }

    private static String[] empty() {
        String[] ids = new String[9];
        Arrays.fill(ids, "empty");
        return ids;
    }

    private static int[] zeroes() {
        return new int[9];
    }

    @Test
    void orbitalOrientationMatches() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[1] = "tnt";
        quantities[1] = 64;
        ids[4] = "rod";
        quantities[4] = 1;
        assertEquals(Match.ORBITAL, RecipeMatcher.match(ids, quantities));
    }

    @Test
    void stabOrientationMatches() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[4] = "rod";
        quantities[4] = 1;
        ids[7] = "tnt";
        quantities[7] = 64;
        assertEquals(Match.STAB, RecipeMatcher.match(ids, quantities));
    }

    @Test
    void orientationsNeverCrossMatch() {
        String[] orbital = empty();
        int[] orbitalCounts = zeroes();
        orbital[1] = "tnt";
        orbitalCounts[1] = 64;
        orbital[4] = "rod";
        orbitalCounts[4] = 1;
        assertEquals(Match.ORBITAL, RecipeMatcher.match(orbital, orbitalCounts));

        String[] stab = empty();
        int[] stabCounts = zeroes();
        stab[4] = "rod";
        stabCounts[4] = 1;
        stab[7] = "tnt";
        stabCounts[7] = 64;
        assertEquals(Match.STAB, RecipeMatcher.match(stab, stabCounts));
    }

    @Test
    void incompleteTntStackIsRejected() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[1] = "tnt";
        quantities[1] = 63;
        ids[4] = "rod";
        quantities[4] = 1;
        assertEquals(Match.NONE, RecipeMatcher.match(ids, quantities));
    }

    @Test
    void singleTntIsRejected() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[4] = "rod";
        quantities[4] = 1;
        ids[7] = "tnt";
        quantities[7] = 1;
        assertEquals(Match.NONE, RecipeMatcher.match(ids, quantities));
    }

    @Test
    void extraItemsAreRejected() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[1] = "tnt";
        quantities[1] = 64;
        ids[4] = "rod";
        quantities[4] = 1;
        ids[0] = "other";
        quantities[0] = 1;
        assertEquals(Match.NONE, RecipeMatcher.match(ids, quantities));
    }

    @Test
    void emptyGridNeverMatches() {
        assertEquals(Match.NONE, RecipeMatcher.match(empty(), zeroes()));
    }

    @Test
    void malformedGridsNeverMatch() {
        assertEquals(Match.NONE, RecipeMatcher.match(null, zeroes()));
        assertEquals(Match.NONE, RecipeMatcher.match(grid("empty"), counts(0)));
    }

    @Test
    void rodAloneNeverMatches() {
        String[] ids = empty();
        int[] quantities = zeroes();
        ids[4] = "rod";
        quantities[4] = 1;
        assertEquals(Match.NONE, RecipeMatcher.match(ids, quantities));
    }
}
