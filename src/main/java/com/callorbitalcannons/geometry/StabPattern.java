package com.callorbitalcannons.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Stab Shot geometry: a full-height vertical column at the target's x/z,
 * swept from the world bottom to the top in 4-block steps with 5 TNT per
 * step at slight vertical offsets so neighbouring blast radii overlap.
 * Each charge uses a 1-tick fuse and no gravity, so the whole column
 * detonates near-simultaneously from the bottom up.
 *
 * <p>1.1.x behavior, per in-game feedback: the column spans the full world
 * height at the target's x/z instead of clustering around the clicked Y —
 * but blast-resistant blocks (≥ 1200) still block the drill, exactly like
 * the reference stopping rule.
 *
 * <p>The sweep runs top-down so the stop triggers on the first resistant
 * layer met from the sky (a bottom-up sweep would halt immediately on the
 * bedrock floor).
 */
public final class StabPattern {
    private StabPattern() {
    }

    /** Downward step between charge groups. */
    public static final int STEP_SIZE = 4;

    /** TNT charges spawned per step. */
    public static final int TNT_PER_STEP = 5;

    /** Vertical spacing between charges inside one step group. */
    public static final double VERTICAL_OFFSET = 0.75;

    /** Reference fuse: near-simultaneous detonation. */
    public static final int FUSE_TICKS = 1;

    /** Blast-resistance stopping threshold (obsidian / ancient debris class). */
    public static final float STOP_RESISTANCE = 1200.0f;

    /** Y offsets of the charges inside one step group, top first. */
    public static List<Double> stepOffsets(int perStep, double verticalOffset) {
        List<Double> offsets = new ArrayList<>(perStep);
        for (int t = 0; t < perStep; t++) {
            offsets.add(0.5 - t * verticalOffset);
        }
        return offsets;
    }

    /** Reference stopping rule: block resists when at or above threshold. */
    public static boolean isStoppingBlock(float blastResistance, float threshold) {
        return blastResistance >= threshold;
    }

    /**
     * Full-column step Y coordinates from {@code maxYInclusive} down to
     * {@code minY}, honouring the configured step size and the maximum
     * number of steps.
     */
    public static List<Integer> fullColumnStepsDown(int maxYInclusive, int minY, int stepSize, int maxSteps) {
        List<Integer> steps = new ArrayList<>();
        int taken = 0;
        for (int y = maxYInclusive; y >= minY && taken < maxSteps; y -= stepSize) {
            steps.add(y);
            taken++;
        }
        return steps;
    }

    /**
     * Full-column step Y coordinates from {@code minY} up to
     * {@code maxYInclusive}, honouring the configured step size and the
     * maximum number of steps.
     */
    public static List<Integer> fullColumnStepsUp(int minY, int maxYInclusive, int stepSize, int maxSteps) {
        List<Integer> steps = new ArrayList<>();
        int taken = 0;
        for (int y = minY; y <= maxYInclusive && taken < maxSteps; y += stepSize) {
            steps.add(y);
            taken++;
        }
        return steps;
    }

    /** Total charges for a column of {@code steps} groups. */
    public static int countForSteps(int steps, int perStep) {
        return steps * perStep;
    }
}
