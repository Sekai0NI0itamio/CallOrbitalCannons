package com.callorbitalcannons.geometry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pure per-player activation cooldown. A strike is allowed when the player
 * has no recorded activation or the configured number of ticks has passed;
 * allowed activations are recorded exactly once by the same call.
 */
public final class CooldownTracker {
    private final Map<UUID, Long> lastFire = new HashMap<>();

    /**
     * Returns true and records {@code tick} when the player may fire.
     * Cooldowns of zero or less always allow firing.
     */
    public boolean tryFire(UUID player, long tick, long cooldownTicks) {
        if (cooldownTicks <= 0) {
            lastFire.put(player, tick);
            return true;
        }
        Long previous = lastFire.get(player);
        if (previous == null || tick - previous >= cooldownTicks) {
            lastFire.put(player, tick);
            return true;
        }
        return false;
    }

    /** Ticks remaining before the player may fire again (0 when ready). */
    public long remaining(UUID player, long tick, long cooldownTicks) {
        Long previous = lastFire.get(player);
        if (previous == null) {
            return 0;
        }
        return Math.max(0, cooldownTicks - (tick - previous));
    }

    public void clear() {
        lastFire.clear();
    }

    public int size() {
        return lastFire.size();
    }
}
