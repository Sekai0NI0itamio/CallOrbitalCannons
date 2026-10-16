package com.callorbitalcannons.geometry;

/**
 * Pure strike-target validation shared by rod activation and the
 * {@code /orbital strike} commands.
 */
public final class StrikeValidator {
    private StrikeValidator() {
    }

    /** True when the target is within {@code maxRange} of the caster. */
    public static boolean withinRange(double px, double py, double pz,
                                      double tx, double ty, double tz, double maxRange) {
        if (maxRange <= 0) {
            return false;
        }
        double dx = tx - px;
        double dy = ty - py;
        double dz = tz - pz;
        return dx * dx + dy * dy + dz * dz <= maxRange * maxRange;
    }

    /** True when every coordinate is finite and Y sits inside the world. */
    public static boolean saneTarget(int x, int y, int z, int minY, int maxYInclusive) {
        return y >= minY && y <= maxYInclusive
                && Math.abs((long) x) <= 30_000_000L
                && Math.abs((long) z) <= 30_000_000L;
    }
}
