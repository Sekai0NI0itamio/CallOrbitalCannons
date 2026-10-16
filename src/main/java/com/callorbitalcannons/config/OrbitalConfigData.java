package com.callorbitalcannons.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure configuration data with validation. File I/O lives in
 * {@link OrbitalConfig}; every rule here is unit tested without Minecraft.
 */
public final class OrbitalConfigData {
    /**
     * Config generation stamp. Bumped whenever defaults change meaning
     * (1.2.1 = generation 2); files without the stamp predate it and are
     * backed up + regenerated on load instead of silently keeping stale
     * gameplay values (e.g. a 1.0.0 stab depth on a full-column build).
     */
    public static final int CURRENT_CONFIG_VERSION = 2;

    public int configVersion = CURRENT_CONFIG_VERSION;
    public double targetingRange = 200.0;
    public int strikeDelayTicks = 10;
    /** Zero disables the per-player recharge (rods are single-use anyway). */
    public long playerCooldownTicks = 0;
    public int maxActiveStrikes = 4;
    public int nukePayloadLimit = 600;
    public int nukeRingCount = 10;
    public double nukeSpawnHeight = 72.0;
    public int nukeFuseTicks = 79;
    public double nukeMisalignRange = 2.0;
    /** Per-charge angular wander, as a fraction of one charge spacing. */
    public double nukeAngularJitter = 0.5;
    public int stabMaxSteps = 64;
    public int stabTntPerStep = 3;
    public int stabStepSize = 8;
    public double stabVerticalOffset = 0.75;
    public int stabFuseTicks = 1;
    public float blastResistanceStop = 1200.0f;
    public boolean terrainDestruction = true;
    public boolean reduceBlockDrops = false;
    public boolean consumeInCreative = false;

    /**
     * Clamps every value into its safe range, repairing malformed files.
     * Returns human-readable warnings for each correction applied.
     */
    public List<String> validate() {
        List<String> warnings = new ArrayList<>();
        targetingRange = clampDouble(warnings, "targetingRange", targetingRange, 16.0, 512.0, 200.0);
        strikeDelayTicks = clampInt(warnings, "strikeDelayTicks", strikeDelayTicks, 0, 1200, 10);
        playerCooldownTicks = clampLong(warnings, "playerCooldownTicks", playerCooldownTicks, 0, 72000, 0);
        maxActiveStrikes = clampInt(warnings, "maxActiveStrikes", maxActiveStrikes, 1, 32, 4);
        nukePayloadLimit = clampInt(warnings, "nukePayloadLimit", nukePayloadLimit, 1, 8000, 600);
        nukeRingCount = clampInt(warnings, "nukeRingCount", nukeRingCount, 1, 10, 10);
        nukeSpawnHeight = clampDouble(warnings, "nukeSpawnHeight", nukeSpawnHeight, 8.0, 320.0, 72.0);
        nukeFuseTicks = clampInt(warnings, "nukeFuseTicks", nukeFuseTicks, 1, 32767, 79);
        nukeMisalignRange = clampDouble(warnings, "nukeMisalignRange", nukeMisalignRange, 0.0, 8.0, 2.0);
        nukeAngularJitter = clampDouble(warnings, "nukeAngularJitter", nukeAngularJitter, 0.0, 1.0, 0.5);
        stabMaxSteps = clampInt(warnings, "stabMaxSteps", stabMaxSteps, 1, 512, 64);
        stabTntPerStep = clampInt(warnings, "stabTntPerStep", stabTntPerStep, 1, 16, 3);
        stabStepSize = clampInt(warnings, "stabStepSize", stabStepSize, 1, 16, 8);
        stabVerticalOffset = clampDouble(warnings, "stabVerticalOffset", stabVerticalOffset, 0.0, 4.0, 0.75);
        stabFuseTicks = clampInt(warnings, "stabFuseTicks", stabFuseTicks, 1, 32767, 1);
        if (blastResistanceStop < 0.0f || Float.isNaN(blastResistanceStop)) {
            warnings.add("blastResistanceStop out of range, reset to 1200.0");
            blastResistanceStop = 1200.0f;
        }
        return warnings;
    }

    /**
     * True when a stored config file predates the current generation and
     * must be backed up + regenerated: blank content, missing version
     * stamp, or older generation. Malformed JSON returns false so the
     * existing {@code .invalid} quarantine path keeps owning it.
     */
    public static boolean needsMigration(String json) {
        if (json == null || json.isBlank()) {
            return true;
        }
        final JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            return false;
        }
        return !root.has("configVersion")
                || root.get("configVersion").getAsInt() != CURRENT_CONFIG_VERSION;
    }

    private static int clampInt(List<String> warnings, String key, int value, int min, int max, int fallback) {
        if (value < min || value > max) {
            warnings.add(key + " out of range [" + min + ", " + max + "], reset to " + fallback);
            return fallback;
        }
        return value;
    }

    private static long clampLong(List<String> warnings, String key, long value, long min, long max, long fallback) {
        if (value < min || value > max) {
            warnings.add(key + " out of range [" + min + ", " + max + "], reset to " + fallback);
            return fallback;
        }
        return value;
    }

    private static double clampDouble(List<String> warnings, String key, double value,
                                       double min, double max, double fallback) {
        if (Double.isNaN(value) || value < min || value > max) {
            warnings.add(key + " out of range [" + min + ", " + max + "], reset to " + fallback);
            return fallback;
        }
        return value;
    }
}
