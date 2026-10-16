package com.callorbitalcannons;

import com.callorbitalcannons.geometry.StabPattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StabPatternTest {
    @Test
    void defaultsMatchReferenceBaseline() {
        assertEquals(5, StabPattern.START_OFFSET_ABOVE);
        assertEquals(4, StabPattern.STEP_SIZE);
        assertEquals(5, StabPattern.TNT_PER_STEP);
        assertEquals(0.75, StabPattern.VERTICAL_OFFSET, 1e-9);
        assertEquals(1, StabPattern.FUSE_TICKS);
        assertEquals(1200.0f, StabPattern.STOP_RESISTANCE, 1e-6f);
    }

    @Test
    void fiveChargesPerStepWithOverlappingOffsets() {
        List<Double> offsets = StabPattern.stepOffsets(5, 0.75);
        assertEquals(List.of(0.5, -0.25, -1.0, -1.75, -2.5), offsets);
        for (int i = 1; i < offsets.size(); i++) {
            double gap = offsets.get(i - 1) - offsets.get(i);
            assertEquals(0.75, gap, 1e-9);
            assertTrue(gap < 4.0, "neighbouring groups must overlap blast radii");
        }
    }

    @Test
    void stoppingRuleMatchesBlastResistantBlocks() {
        assertTrue(StabPattern.isStoppingBlock(1200.0f, 1200.0f));
        assertTrue(StabPattern.isStoppingBlock(3600000.0f, 1200.0f));
        assertFalse(StabPattern.isStoppingBlock(1199.9f, 1200.0f));
        assertFalse(StabPattern.isStoppingBlock(6.0f, 1200.0f));
    }

    @Test
    void columnStepsDescendInFourBlockIncrements() {
        List<Integer> steps = StabPattern.columnSteps(69, -64, 4, 80);
        assertFalse(steps.isEmpty());
        assertEquals(69, steps.get(0));
        for (int i = 1; i < steps.size(); i++) {
            assertEquals(4, steps.get(i - 1) - steps.get(i));
        }
        assertTrue(steps.get(steps.size() - 1) >= -64);
    }

    @Test
    void maxStepsCapsColumnDepth() {
        List<Integer> steps = StabPattern.columnSteps(100, -64, 4, 3);
        assertEquals(List.of(100, 96, 92), steps);
        assertEquals(15, StabPattern.countForSteps(steps.size(), 5));
    }
}
