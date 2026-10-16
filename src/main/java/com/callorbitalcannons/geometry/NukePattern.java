package com.callorbitalcannons.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure Nuke (Orbital) Shot geometry, ported from the RelmesStudios
 * {@code orbitalstrike} reference ({@code StrikeManager.spawnNuke}).
 *
 * <p>Layout: one stationary center charge plus ten concentric rings at radii
 * 6..51 (step 5). Every ring charge spawns at the same point 72 blocks above
 * the target with an initial horizontal velocity chosen so that, after
 * {@code fuseTicks} of TNT drag (0.98 per tick), it lands at its assigned
 * ring position. A small uniform-disc misalignment is applied per charge and
 * clamped so each charge stays inside its ring band.
 *
 * <p>No Minecraft classes are referenced here so the math stays unit
 * testable; the Fabric modules translate placements into entities.
 */
public final class NukePattern {
    private NukePattern() {
    }

    /** Ring radii in blocks, innermost first. */
    public static final double[] RING_RADII = {6.0, 11.0, 16.0, 21.0, 26.0, 31.0, 36.0, 41.0, 46.0, 51.0};

    /**
     * Charges per ring, verbatim from the reference {@code ringTntCounts}.
     * Sums to 668 ring charges; plus the center charge the reference spawns
     * 669 TNT (see docs/REFERENCE_RESEARCH.md for the 661-vs-669 discrepancy).
     */
    public static final int[] RING_COUNTS = {15, 27, 38, 49, 62, 73, 83, 95, 107, 119};

    /** Spawn height above the target block. */
    public static final double SPAWN_HEIGHT = 72.0;

    /** TNT fuse in ticks. */
    public static final int FUSE_TICKS = 79;

    /** Per-tick horizontal velocity retention of a primed TNT entity. */
    public static final double DRAG = 0.98;

    /** Reference default misalignment disc radius. */
    public static final double MISALIGN_RANGE = 0.5;

    /** Placement of one ring charge relative to the target center. */
    public record Placement(double dx, double dz, double vx, double vz) {
    }

    /** Sum of {@link #RING_COUNTS}. */
    public static int ringTotal() {
        int total = 0;
        for (int count : RING_COUNTS) {
            total += count;
        }
        return total;
    }

    /** Ring charges plus the single stationary center charge. */
    public static int totalPayload() {
        return ringTotal() + 1;
    }

    /**
     * Drag displacement scale: a charge with initial horizontal velocity
     * {@code v} travels {@code v * scale} blocks over {@code fuseTicks}.
     * Closed form of the geometric series v * (1 - drag^fuse) / (1 - drag).
     */
    public static double dragScale(int fuseTicks) {
        return (1.0 - Math.pow(DRAG, fuseTicks)) / (1.0 - DRAG);
    }

    /** Initial velocity components needed to land at (dx, dz). */
    public static double[] velocityFor(double dx, double dz, int fuseTicks) {
        double scale = dragScale(fuseTicks);
        return new double[]{dx / scale, dz / scale};
    }

    /**
     * Clamps (x, z) into the band of {@code ringIndex}: halfway to each
     * neighbouring ring, or the documented edge rule for the first/last ring.
     */
    public static double[] clampToBand(double x, double z, int ringIndex, double[] radii, double misalign) {
        double baseRadius = radii[ringIndex];
        double minRadius = ringIndex == 0 ? 0.0 : (radii[ringIndex - 1] + baseRadius) / 2.0;
        double maxRadius = ringIndex == radii.length - 1
                ? baseRadius + misalign
                : (baseRadius + radii[ringIndex + 1]) / 2.0;
        double radius = Math.sqrt(x * x + z * z);
        if (radius < minRadius || radius > maxRadius) {
            double clamped = Math.max(minRadius, Math.min(maxRadius, radius));
            double scale = radius == 0.0 ? 0.0 : clamped / radius;
            return new double[]{x * scale, z * scale};
        }
        return new double[]{x, z};
    }

    /**
     * Generates every ring placement for one strike using the reference
     * algorithm: even angular spacing, uniform-disc misalignment, band clamp,
     * drag-compensated velocity.
     */
    public static List<Placement> generatePlacements(Random random, double[] radii, int[] counts,
                                                     double misalign, int fuseTicks) {
        List<Placement> out = new ArrayList<>();
        for (int i = 0; i < radii.length; i++) {
            double baseRadius = radii[i];
            int tntCount = counts[i];
            for (int j = 0; j < tntCount; j++) {
                double angle = (2.0 * Math.PI * j) / tntCount;
                double offsetAngle = random.nextDouble() * 2.0 * Math.PI;
                double offsetMag = Math.sqrt(random.nextDouble()) * misalign;
                double idealX = baseRadius * Math.cos(angle);
                double idealZ = baseRadius * Math.sin(angle);
                double[] clamped = clampToBand(idealX + offsetMag * Math.cos(offsetAngle),
                        idealZ + offsetMag * Math.sin(offsetAngle), i, radii, misalign);
                double[] velocity = velocityFor(clamped[0], clamped[1], fuseTicks);
                out.add(new Placement(clamped[0], clamped[1], velocity[0], velocity[1]));
            }
        }
        return out;
    }
}
