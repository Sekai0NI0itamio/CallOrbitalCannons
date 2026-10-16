package com.callorbitalcannons;

import com.callorbitalcannons.geometry.NukePattern;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NukePatternTest {
    @Test
    void ringRadiiAreDocumentedBaseline() {
        assertArrayEquals(new double[]{6.0, 11.0, 16.0, 21.0, 26.0, 31.0, 36.0, 41.0, 46.0, 51.0},
                NukePattern.RING_RADII, 1e-9);
    }

    @Test
    void ringCountsAreVerbatimReferenceValues() {
        assertArrayEquals(new int[]{15, 27, 38, 49, 62, 73, 83, 95, 107, 119}, NukePattern.RING_COUNTS);
    }

    @Test
    void ringTotalsMatchReferenceSource() {
        assertEquals(668, NukePattern.ringTotal());
        assertEquals(669, NukePattern.totalPayload());
    }

    @Test
    void defaultsMatchReferenceBaseline() {
        assertEquals(72.0, NukePattern.SPAWN_HEIGHT, 1e-9);
        assertEquals(79, NukePattern.FUSE_TICKS);
        assertEquals(0.5, NukePattern.MISALIGN_RANGE, 1e-9);
        assertEquals(0.98, NukePattern.DRAG, 1e-9);
    }

    @Test
    void dragScaleMatchesGeometricSeries() {
        double expected = 0.0;
        double velocity = 1.0;
        for (int i = 0; i < NukePattern.FUSE_TICKS; i++) {
            expected += velocity;
            velocity *= NukePattern.DRAG;
        }
        assertEquals(expected, NukePattern.dragScale(NukePattern.FUSE_TICKS), 1e-6);
    }

    @Test
    void velocityLandsOnTargetAfterDrag() {
        double dx = 51.0;
        double dz = -11.0;
        double[] velocity = NukePattern.velocityFor(dx, dz, NukePattern.FUSE_TICKS);
        double x = 0.0;
        double z = 0.0;
        double vx = velocity[0];
        double vz = velocity[1];
        for (int i = 0; i < NukePattern.FUSE_TICKS; i++) {
            x += vx;
            z += vz;
            vx *= NukePattern.DRAG;
            vz *= NukePattern.DRAG;
        }
        assertEquals(dx, x, 1e-6);
        assertEquals(dz, z, 1e-6);
    }

    @Test
    void placementsCoverEveryRingCharge() {
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(new Random(1234),
                NukePattern.RING_RADII, NukePattern.RING_COUNTS,
                NukePattern.MISALIGN_RANGE, NukePattern.FUSE_TICKS);
        assertEquals(NukePattern.ringTotal(), placements.size());
    }

    @Test
    void placementsStayInsideRingBands() {
        Random random = new Random(99);
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(random,
                NukePattern.RING_RADII, NukePattern.RING_COUNTS,
                NukePattern.MISALIGN_RANGE, NukePattern.FUSE_TICKS);
        int cursor = 0;
        for (int ring = 0; ring < NukePattern.RING_RADII.length; ring++) {
            double base = NukePattern.RING_RADII[ring];
            double min = ring == 0 ? 0.0 : (NukePattern.RING_RADII[ring - 1] + base) / 2.0;
            double max = ring == NukePattern.RING_RADII.length - 1
                    ? base + NukePattern.MISALIGN_RANGE
                    : (base + NukePattern.RING_RADII[ring + 1]) / 2.0;
            for (int j = 0; j < NukePattern.RING_COUNTS[ring]; j++) {
                NukePattern.Placement placement = placements.get(cursor++);
                double radius = Math.hypot(placement.dx(), placement.dz());
                assertTrue(radius >= min - 1e-9 && radius <= max + 1e-9,
                        "ring " + ring + " radius " + radius + " outside [" + min + ", " + max + "]");
            }
        }
    }

    @Test
    void zeroMisalignmentLandsExactlyOnRings() {
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(new Random(7),
                NukePattern.RING_RADII, NukePattern.RING_COUNTS, 0.0, NukePattern.FUSE_TICKS);
        int cursor = 0;
        for (int ring = 0; ring < NukePattern.RING_RADII.length; ring++) {
            for (int j = 0; j < NukePattern.RING_COUNTS[ring]; j++) {
                NukePattern.Placement placement = placements.get(cursor++);
                assertEquals(NukePattern.RING_RADII[ring], Math.hypot(placement.dx(), placement.dz()), 1e-6);
            }
        }
    }
}
