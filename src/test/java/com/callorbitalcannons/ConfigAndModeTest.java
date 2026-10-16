package com.callorbitalcannons;

import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.item.WeaponMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(600, data.nukePayloadLimit);
        assertEquals(10, data.nukeRingCount);
        assertEquals(72.0, data.nukeSpawnHeight, 1e-9);
        assertEquals(79, data.nukeFuseTicks);
        assertEquals(2.0, data.nukeMisalignRange, 1e-9);
        assertEquals(0.5, data.nukeAngularJitter, 1e-9);
        assertEquals(64, data.stabMaxSteps);
        assertEquals(3, data.stabTntPerStep);
        assertEquals(8, data.stabStepSize);
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
        data.nukeAngularJitter = 2.0;
        data.stabMaxSteps = 0;
        data.blastResistanceStop = Float.NaN;
        assertEquals(9, data.validate().size());
        assertEquals(200.0, data.targetingRange, 1e-9);
        assertEquals(10, data.strikeDelayTicks);
        assertEquals(4, data.maxActiveStrikes);
        assertEquals(10, data.nukeRingCount);
        assertEquals(79, data.nukeFuseTicks);
        assertEquals(600, data.nukePayloadLimit);
        assertEquals(0.5, data.nukeAngularJitter, 1e-9);
        assertEquals(64, data.stabMaxSteps);
        assertEquals(1200.0f, data.blastResistanceStop, 1e-6f);
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
