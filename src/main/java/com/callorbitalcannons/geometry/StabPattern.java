package com.callorbitalcannons.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Stab Shot geometry, ported from the reference
 * {@code StrikeManager.spawnStab}: a vertical column starting 5 blocks above
 * the target, stepping down every 4 blocks with 5 TNT per step at slight
 * vertical offsets so neighbouring blast radii overlap. Each charge uses a
 * 1-tick fuse and no gravity; the column stops at blast resistance >= 1200.
 */
public final class StabPattern {
    private StabPattern() {
    }

    /** Column starts this many blocks above the targeted block. */
    public static final int START_OFFSET_ABOVE = 5;

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
     * Step Y coordinates from {@code startY} downward, honouring world bottom
     * and the configured maximum number of steps.
     */
    public static List<Integer> columnSteps(int startY, int minY, int stepSize, int maxSteps) {
        List<Integer> steps = new ArrayList<>();
        int taken = 0;
        for (int y = startY; y >= minY && taken < maxSteps; y -= stepSize) {
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
