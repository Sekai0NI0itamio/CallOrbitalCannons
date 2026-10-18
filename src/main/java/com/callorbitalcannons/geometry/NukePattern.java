package com.callorbitalcannons.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Nuke (Orbital) Shot geometry: one stationary center charge plus ten
 * concentric rings dropped from high above with drag-compensated outward
 * velocity, so the charges spread back out to their ring radii as they fall.
 *
 * <p>1.2.9 behavior: the cubicmetre Mk.6 payload formation with front-loaded
 * inner rings — ring charge counts are proportional to radius
 * (round(2.5 × radius)) but weighted toward the center:
 * {24, 48, 56, 52, 65, 78, 90, 103, 115, 128} for radii 6..51 — 759 ring
 * charges + 1 center = 760 TNT over a ~102-block footprint. The first three
 * rings sit densest (2nd ring packed tight) so the strike opens with impact;
 * outer rings keep breathing room. Every charge is placed at a random angle
 * around its ring with a tiny 0.2-block scatter disc. This matches
 * cubicmetre's Mk.6 cannon: prime momentum kicks each TNT in a random
 * direction, so rings render as dense clumps rather than even spacing.
 * Counts, jitter, scatter, and ring count remain configurable; see
 * docs/REFERENCE_RESEARCH.md §13.
 *
 * <p>No Minecraft classes are referenced here so the math stays unit
 * testable; the Fabric modules translate placements into entities.
 */
public final class NukePattern {
    private NukePattern() {
    }

    /** Ring radii in blocks, innermost first (uniform 5-block gaps; the staged spec's 10 rings). */
    public static final double[] RING_RADII = {6.0, 11.0, 16.0, 21.0, 26.0, 31.0, 36.0, 41.0, 46.0, 51.0};

    /**
     * Charges per ring (cubicmetre Mk.6 law, front-loaded: the amount of TNT
     * added to a ring is proportional to its radius, round(2.5 × radius), but
     * the first three rings are densest — 24, 48, 56 — so the strike opens
     * tight. Sums to 759 ring charges; plus the center charge each strike
     * uses 760 TNT over a ~102-block footprint. With random placement (the
     * default) rings render as dense clumps with gaps; inner rings are
     * denser per unit area than outer ones — matching cubicmetre's Mk.6
     * payload render.
     */
    public static final int[] RING_COUNTS = {24, 48, 56, 52, 65, 78, 90, 103, 115, 128};

    /** Spawn height above the target block. */
    public static final double SPAWN_HEIGHT = 72.0;

    /** TNT fuse in ticks. */
    public static final int FUSE_TICKS = 79;

    /** Per-tick horizontal velocity retention of a primed TNT entity. */
    public static final double DRAG = 0.98;

    /** Default scatter-disc radius per charge, in blocks (flat for every ring — cubicmetre's Mk.6 prime-momentum kick is tiny, ~0.2 blocks). */
    public static final double MISALIGN_RANGE = 0.2;

    /**
     * Default angular jitter as a fraction of one charge spacing (1.0 =
     * random: cubicmetre's Mk.6 rings place charges at random angles, so
     * rings render as dense clumps rather than even spacing; 0.0 = even
     * rings).
     */
    public static final double ANGULAR_JITTER = 1.0;

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
     * Generates every ring placement for one strike: angular jitter around
     * the even spacing (default 1.0 = random angles per charge), uniform-disc
     * scatter, band clamp, then drag-compensated velocity toward the final
     * offset.
     */
    public static List<Placement> generatePlacements(Random random, double[] radii, int[] counts,
                                                     double misalign, int fuseTicks, double angularJitter) {
        List<Placement> out = new ArrayList<>();
        for (int i = 0; i < radii.length; i++) {
            double baseRadius = radii[i];
            int tntCount = counts[i];
            double spacing = (2.0 * Math.PI) / tntCount;
            for (int j = 0; j < tntCount; j++) {
                double angle = spacing * j + (random.nextDouble() * 2.0 - 1.0) * angularJitter * spacing;

                // Circular randomness: uniform point in a disc of radius misalign.
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
