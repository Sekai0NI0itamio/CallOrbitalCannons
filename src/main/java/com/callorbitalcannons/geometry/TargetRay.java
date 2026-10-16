package com.callorbitalcannons.geometry;

import java.util.Optional;

/**
 * Long-range targeting fallback: when the exact raycast misses, march the
 * look ray forward and approximate the target at the last loaded voxel —
 * the nearest block position to what the player was aiming at.
 *
 * <p>A clean miss (ray stays inside loaded chunks for the whole range, e.g.
 * aiming at the sky) yields {@link Optional#empty()} so the rod is kept.
 * Only a ray that actually leaves loaded territory mid-flight produces an
 * approximate target. No Minecraft classes are referenced here so the march
 * stays unit testable; the handler supplies the chunk view.
 */
public final class TargetRay {
    private TargetRay() {
    }

    /** Chunk-load probe supplied by the caller. */
    public interface ChunkView {
        boolean isLoaded(int x, int y, int z);
    }

    /** Approximate target: block voxel plus whether it is exact or not. */
    public record Target(int x, int y, int z, boolean approximate) {
    }

    /** March step in blocks; half-block sampling never skips a voxel. */
    static final double STEP = 0.5;

    /**
     * Marches from the eye position along {@code (dx, dy, dz)} (need not be
     * unit length) up to {@code maxDist} blocks. Returns an approximate
     * target at the last loaded voxel when the ray leaves loaded territory,
     * clamped into {@code [minY, maxYInclusive]}; otherwise empty.
     */
    public static Optional<Target> approximate(double ex, double ey, double ez,
                                              double dx, double dy, double dz,
                                              double maxDist, int minY, int maxYInclusive,
                                              ChunkView chunks) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0 || maxDist <= 0.0) {
            return Optional.empty();
        }
        double ux = dx / length;
        double uy = dy / length;
        double uz = dz / length;

        int[] lastLoaded = null;
        boolean sawUnloaded = false;
        for (double d = STEP; d <= maxDist; d += STEP) {
            int x = (int) Math.floor(ex + ux * d);
            int y = (int) Math.floor(ey + uy * d);
            int z = (int) Math.floor(ez + uz * d);
            if (chunks.isLoaded(x, y, z)) {
                lastLoaded = new int[]{x, y, z};
            } else {
                sawUnloaded = true;
            }
        }
        if (!sawUnloaded || lastLoaded == null) {
            return Optional.empty();
        }
        int y = Math.max(minY, Math.min(maxYInclusive, lastLoaded[1]));
        return Optional.of(new Target(lastLoaded[0], y, lastLoaded[2], true));
    }
}
