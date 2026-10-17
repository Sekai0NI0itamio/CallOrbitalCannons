package com.callorbitalcannons.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Nuke (Orbital) Shot geometry: one stationary center charge plus eleven
 * concentric rings dropped from high above with drag-compensated outward
 * velocity, so the charges spread back out to their ring radii as they fall.
 *
 * <p>1.2.5 behavior, shaped to the manually counted Wemmbbu formation
 * (11 rings, 3.4-block ring gaps, equal charges on every ring): a
 * 1101-charge pattern over radii 6.0..40.0 — one center charge plus eleven
 * rings of 100 charges each. The uniform per-ring count is derived from the
 * outermost ring: circumference 2π×40 ≈ 251 blocks over the observed
 * 2.5-block charge spacing (1–2-block visible gaps) ≈ 100. Because the
 * circumference grows outward while the count stays fixed, inner rings read
 * solid while the outer fringe sits pointillist (~2.5 blocks apart).
 * Angular jitter and scatter are scaled per ring and kept low (~80-95%
 * circular even on the outer fringe). Counts, jitter, and scatter are all
 * configurable; see docs/REFERENCE_RESEARCH.md for provenance.
 *
 * <p>No Minecraft classes are referenced here so the math stays unit
 * testable; the Fabric modules translate placements into entities.
 */
public final class NukePattern {
    private NukePattern() {
    }

    /** Ring radii in blocks, innermost first (uniform 3.4-block gaps). */
    public static final double[] RING_RADII = {6.0, 9.4, 12.8, 16.2, 19.6, 23.0, 26.4, 29.8, 33.2, 36.6, 40.0};

    /**
     * Charges per ring. Sums to 1100 ring charges; plus the center charge
     * each strike uses 1101 TNT. Every ring carries the same 100 charges —
     * the outermost-ring count derived from the observed 1–2 block charge
     * gaps (2π×40.0 / 2.5 ≈ 100) — so inner rings pack solid and the outer
     * fringe reads pointillist.
     */
    public static final int[] RING_COUNTS = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};

    /** Spawn height above the target block. */
    public static final double SPAWN_HEIGHT = 72.0;

    /** TNT fuse in ticks. */
    public static final int FUSE_TICKS = 79;

    /** Per-tick horizontal velocity retention of a primed TNT entity. */
    public static final double DRAG = 0.98;

    /** Default scatter-disc radius per charge, in blocks (applied fully to the outermost ring; scaled down for inner rings). */
    public static final double MISALIGN_RANGE = 1.0;

    /**
     * Default angular jitter as a fraction of one charge spacing (applied fully to the outermost ring;
     * scaled down for inner rings). Each charge's angle wanders up to ± this fraction of the even
     * spacing, which breaks up the perfect-circle look.
     */
    public static final double ANGULAR_JITTER = 0.2;

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
     * neighbouring ring, or the edge rule for the first/last ring.
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
     * Generates every ring placement for one strike: jittered angle around
     * the even spacing, uniform-disc scatter, band clamp, then
     * drag-compensated velocity toward the final offset.
     */
    public static List<Placement> generatePlacements(Random random, double[] radii, int[] counts,
                                                     double misalign, int fuseTicks, double angularJitter) {
        List<Placement> out = new ArrayList<>();
        for (int i = 0; i < radii.length; i++) {
            double baseRadius = radii[i];
            int tntCount = counts[i];
            // Rings stay mostly circular everywhere (~80-95%): a slight
            // upward ramp toward the outer fringe keeps bands clean but
            // not rigid, so even the last rings are just lightly scattered.
            double ringProgress = radii.length == 1 ? 0.0 : (double) i / (radii.length - 1);
            double messiness = 0.1 + 0.1 * ringProgress;
            double ringJitter = angularJitter * messiness;
            double ringMisalign = misalign * messiness;
            double spacing = (2.0 * Math.PI) / tntCount;
            for (int j = 0; j < tntCount; j++) {
                double angle = spacing * j + (random.nextDouble() * 2.0 - 1.0) * ringJitter * spacing;

                // Circular randomness: uniform point in a disc of radius ringMisalign.
                double offsetAngle = random.nextDouble() * 2.0 * Math.PI;
                double offsetMag = Math.sqrt(random.nextDouble()) * ringMisalign;

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
