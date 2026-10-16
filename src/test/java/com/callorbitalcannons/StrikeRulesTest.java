package com.callorbitalcannons;

import com.callorbitalcannons.geometry.CooldownTracker;
import com.callorbitalcannons.geometry.StrikeValidator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrikeRulesTest {
    @Test
    void firstActivationIsAllowed() {
        CooldownTracker tracker = new CooldownTracker();
        assertTrue(tracker.tryFire(UUID.randomUUID(), 100L, 100L));
    }

    @Test
    void repeatActivationInsideCooldownIsDenied() {
        CooldownTracker tracker = new CooldownTracker();
        UUID player = UUID.randomUUID();
        assertTrue(tracker.tryFire(player, 100L, 100L));
        assertFalse(tracker.tryFire(player, 150L, 100L));
        assertEquals(50L, tracker.remaining(player, 150L, 100L));
    }

    @Test
    void activationAfterCooldownIsAllowedOnce() {
        CooldownTracker tracker = new CooldownTracker();
        UUID player = UUID.randomUUID();
        assertTrue(tracker.tryFire(player, 100L, 100L));
        assertTrue(tracker.tryFire(player, 200L, 100L));
        assertFalse(tracker.tryFire(player, 201L, 100L));
    }

    @Test
    void differentPlayersHaveIndependentCooldowns() {
        CooldownTracker tracker = new CooldownTracker();
        assertTrue(tracker.tryFire(UUID.randomUUID(), 100L, 100L));
        assertTrue(tracker.tryFire(UUID.randomUUID(), 100L, 100L));
        assertEquals(2, tracker.size());
    }

    @Test
    void zeroCooldownAlwaysAllows() {
        CooldownTracker tracker = new CooldownTracker();
        UUID player = UUID.randomUUID();
        assertTrue(tracker.tryFire(player, 5L, 0L));
        assertTrue(tracker.tryFire(player, 5L, 0L));
    }

    @Test
    void clearResetsAllCooldowns() {
        CooldownTracker tracker = new CooldownTracker();
        UUID player = UUID.randomUUID();
        tracker.tryFire(player, 100L, 100L);
        tracker.clear();
        assertEquals(0, tracker.size());
        assertEquals(0L, tracker.remaining(player, 150L, 100L));
        assertTrue(tracker.tryFire(player, 150L, 100L));
    }

    @Test
    void rangeBoundaryIsRespected() {
        assertTrue(StrikeValidator.withinRange(0, 64, 0, 200, 64, 0, 200.0));
        assertFalse(StrikeValidator.withinRange(0, 64, 0, 201, 64, 0, 200.0));
        assertFalse(StrikeValidator.withinRange(0, 64, 0, 10, 64, 0, 0.0));
    }

    @Test
    void invalidTargetsAreRejected() {
        assertTrue(StrikeValidator.saneTarget(100, 64, -200, -64, 319));
        assertFalse(StrikeValidator.saneTarget(0, -65, 0, -64, 319));
        assertFalse(StrikeValidator.saneTarget(0, 320, 0, -64, 319));
        assertFalse(StrikeValidator.saneTarget(30_000_001, 64, 0, -64, 319));
    }
}
