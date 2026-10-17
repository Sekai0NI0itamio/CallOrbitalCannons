package com.callorbitalcannons;

import com.callorbitalcannons.geometry.StabPattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StabPatternTest {
    @Test
    void defaultsMatchTunedBaseline() {
        assertEquals(2, StabPattern.STEP_SIZE);
        assertEquals(1, StabPattern.TNT_PER_STEP);
        assertEquals(0.75, StabPattern.VERTICAL_OFFSET, 1e-9);
        assertEquals(1, StabPattern.FUSE_TICKS);
    }

    @Test
    void threeChargesPerStepWithOverlappingOffsets() {
        List<Double> offsets = StabPattern.stepOffsets(3, 0.75);
        assertEquals(List.of(0.5, -0.25, -1.0), offsets);
        for (int i = 1; i < offsets.size(); i++) {
            double gap = offsets.get(i - 1) - offsets.get(i);
            assertEquals(0.75, gap, 1e-9);
        }
    }

    @Test
    void fullColumnSweepsBottomToTop() {
        List<Integer> steps = StabPattern.fullColumnStepsUp(-64, 319, 2, 192);
        assertFalse(steps.isEmpty());
        assertEquals(-64, steps.get(0));
        for (int i = 1; i < steps.size(); i++) {
            assertEquals(2, steps.get(i) - steps.get(i - 1));
        }
        assertTrue(steps.get(steps.size() - 1) <= 319);
        assertEquals(192, steps.size());
        assertEquals(192, StabPattern.countForSteps(steps.size(), 1));
    }

    @Test
    void fullColumnDrillRunsTopDown() {
        List<Integer> steps = StabPattern.fullColumnStepsDown(319, -64, 2, 192);
        assertFalse(steps.isEmpty());
        assertEquals(319, steps.get(0));
        for (int i = 1; i < steps.size(); i++) {
            assertEquals(2, steps.get(i - 1) - steps.get(i));
        }
        assertTrue(steps.get(steps.size() - 1) >= -64);
        assertEquals(192, steps.size());
    }

    @Test
    void stoppingRuleMatchesBlastResistantBlocks() {
        assertTrue(StabPattern.isStoppingBlock(1200.0f, 1200.0f));
        assertTrue(StabPattern.isStoppingBlock(3600000.0f, 1200.0f));
        assertFalse(StabPattern.isStoppingBlock(1199.9f, 1200.0f));
        assertFalse(StabPattern.isStoppingBlock(6.0f, 1200.0f));
    }

    @Test
    void maxStepsCapsColumnDepth() {
        List<Integer> steps = StabPattern.fullColumnStepsUp(-64, 319, 2, 3);
        assertEquals(List.of(-64, -62, -60), steps);
        assertEquals(3, StabPattern.countForSteps(steps.size(), 1));
    }
}
