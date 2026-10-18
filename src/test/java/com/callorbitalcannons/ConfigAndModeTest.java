package com.callorbitalcannons;

import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.item.WeaponMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigAndModeTest {
    @Test
    void defaultsPassValidationUntouched() {
        OrbitalConfigData data = new OrbitalConfigData();
        assertTrue(data.validate().isEmpty());
        assertEquals(200.0, data.targetingRange, 1e-9);
        assertEquals(10, data.strikeDelayTicks);
        assertEquals(0L, data.playerCooldownTicks);
        assertEquals(4, data.maxActiveStrikes);
        assertEquals(800, data.nukePayloadLimit);
        assertEquals(10, data.nukeRingCount);
        assertEquals(72.0, data.nukeSpawnHeight, 1e-9);
        assertEquals(79, data.nukeFuseTicks);
        assertEquals(0.2, data.nukeMisalignRange, 1e-9);
        assertEquals(1.0, data.nukeAngularJitter, 1e-9);
        assertEquals(192, data.stabMaxSteps);
        assertEquals(1, data.stabTntPerStep);
        assertEquals(2, data.stabStepSize);
        assertEquals(1200.0f, data.blastResistanceStop, 1e-6f);
    }

    @Test
    void malformedValuesAreRepairedWithWarnings() {
        OrbitalConfigData data = new OrbitalConfigData();
        data.targetingRange = -5.0;
        data.strikeDelayTicks = -1;
        data.maxActiveStrikes = 0;
        data.nukeRingCount = 99;
        data.nukeFuseTicks = 0;
        data.nukePayloadLimit = 0;
        data.nukeAngularJitter = 5.0;
        data.stabMaxSteps = 0;
        data.blastResistanceStop = Float.NaN;
        assertEquals(9, data.validate().size());
        assertEquals(200.0, data.targetingRange, 1e-9);
        assertEquals(10, data.strikeDelayTicks);
        assertEquals(4, data.maxActiveStrikes);
        assertEquals(10, data.nukeRingCount);
        assertEquals(79, data.nukeFuseTicks);
        assertEquals(800, data.nukePayloadLimit);
        assertEquals(1.0, data.nukeAngularJitter, 1e-9);
        assertEquals(192, data.stabMaxSteps);
        assertEquals(1200.0f, data.blastResistanceStop, 1e-6f);
    }

    @Test
    void freshDefaultsCarryCurrentVersion() {
        assertEquals(OrbitalConfigData.CURRENT_CONFIG_VERSION, new OrbitalConfigData().configVersion);
    }

    @Test
    void preStampConfigNeedsMigration() {
        // 1.0.0-era file: no version stamp, stale stab depth.
        assertTrue(OrbitalConfigData.needsMigration(
                "{\"targetingRange\":200.0,\"stabMaxSteps\":80,\"stabStepSize\":4}"));
    }

    @Test
    void currentStampedConfigNeedsNoMigration() {
        assertFalse(OrbitalConfigData.needsMigration(
                "{\"configVersion\":" + OrbitalConfigData.CURRENT_CONFIG_VERSION + ",\"stabMaxSteps\":64}"));
    }

    @Test
    void olderStampNeedsMigration() {
        assertTrue(OrbitalConfigData.needsMigration(null));
        assertTrue(OrbitalConfigData.needsMigration(""));
        assertTrue(OrbitalConfigData.needsMigration(
                "{\"configVersion\":" + (OrbitalConfigData.CURRENT_CONFIG_VERSION - 1) + "}"));
    }

    @Test
    void malformedJsonStaysOnInvalidPath() {
        assertFalse(OrbitalConfigData.needsMigration("{not json"));
    }

    @Test
    void weaponModesParseById() {
        assertEquals(WeaponMode.NUKE, WeaponMode.fromId("nuke"));
        assertEquals(WeaponMode.STAB, WeaponMode.fromId("stab"));
        assertEquals(WeaponMode.NUKE, WeaponMode.fromId("NUKE"));
        assertNull(WeaponMode.fromId("strike"));
        assertNull(WeaponMode.fromId(""));
        assertNull(WeaponMode.fromId(null));
    }

    @Test
    void displayNamesAreDistinct() {
        assertEquals("Orbital Fishing Rod", WeaponMode.NUKE.displayName());
        assertEquals("Stab Shot Fishing Rod", WeaponMode.STAB.displayName());
    }
}
