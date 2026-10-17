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
        assertArrayEquals(new double[]{6.0, 13.0, 20.0, 27.0, 34.0, 41.0, 48.0, 55.0, 62.0, 69.0},
                NukePattern.RING_RADII, 1e-9);
    }

    @Test
    void ringGapsAreWideAndUniform() {
        for (int i = 1; i < NukePattern.RING_RADII.length; i++) {
            assertEquals(7.0, NukePattern.RING_RADII[i] - NukePattern.RING_RADII[i - 1], 1e-9);
        }
    }

    @Test
    void ringCountsGiveThinnedPayload() {
        assertArrayEquals(new int[]{30, 102, 84, 68, 60, 52, 44, 38, 32, 28}, NukePattern.RING_COUNTS);
    }

    @Test
    void ringTotalsMatchFiveThirtyNineBaseline() {
        assertEquals(538, NukePattern.ringTotal());
        assertEquals(539, NukePattern.totalPayload());
    }

    @Test
    void secondRingIsDensestThenDecreasingOutward() {
        int[] counts = NukePattern.RING_COUNTS;
        double[] radii = NukePattern.RING_RADII;
        double secondSpacing = 2.0 * Math.PI * radii[1] / counts[1];
        for (int i = 0; i < counts.length; i++) {
            if (i == 1) {
                continue;
            }
            double spacing = 2.0 * Math.PI * radii[i] / counts[i];
            assertTrue(spacing > secondSpacing, "ring " + i + " should be sparser than ring 1");
        }
        for (int i = 2; i < counts.length; i++) {
            assertTrue(counts[i] < counts[i - 1], "ring " + i + " should be sparser than ring " + (i - 1));
        }
    }

    @Test
    void defaultsMatchTunedBaseline() {
        assertEquals(72.0, NukePattern.SPAWN_HEIGHT, 1e-9);
        assertEquals(79, NukePattern.FUSE_TICKS);
        assertEquals(1.5, NukePattern.MISALIGN_RANGE, 1e-9);
        assertEquals(0.3, NukePattern.ANGULAR_JITTER, 1e-9);
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
                NukePattern.MISALIGN_RANGE, NukePattern.FUSE_TICKS, NukePattern.ANGULAR_JITTER);
        assertEquals(NukePattern.ringTotal(), placements.size());
    }

    @Test
    void placementsStayInsideRingBands() {
        Random random = new Random(99);
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(random,
                NukePattern.RING_RADII, NukePattern.RING_COUNTS,
                NukePattern.MISALIGN_RANGE, NukePattern.FUSE_TICKS, NukePattern.ANGULAR_JITTER);
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
    void zeroScatterAndJitterLandsExactlyOnRings() {
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(new Random(7),
                NukePattern.RING_RADII, NukePattern.RING_COUNTS, 0.0, NukePattern.FUSE_TICKS, 0.0);
        int cursor = 0;
        for (int ring = 0; ring < NukePattern.RING_RADII.length; ring++) {
            for (int j = 0; j < NukePattern.RING_COUNTS[ring]; j++) {
                NukePattern.Placement placement = placements.get(cursor++);
                assertEquals(NukePattern.RING_RADII[ring], Math.hypot(placement.dx(), placement.dz()), 1e-6);
            }
        }
    }

    @Test
    void innerRingsStayTighterThanOuterRings() {
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(new Random(42),
                NukePattern.RING_RADII, NukePattern.RING_COUNTS,
                NukePattern.MISALIGN_RANGE, NukePattern.FUSE_TICKS, NukePattern.ANGULAR_JITTER);
        int cursor = 0;
        double[] rmsDev = new double[NukePattern.RING_RADII.length];
        for (int ring = 0; ring < NukePattern.RING_RADII.length; ring++) {
            double base = NukePattern.RING_RADII[ring];
            double sumSq = 0.0;
            for (int j = 0; j < NukePattern.RING_COUNTS[ring]; j++) {
                NukePattern.Placement p = placements.get(cursor++);
                double r = Math.hypot(p.dx(), p.dz());
                double d = r - base;
                sumSq += d * d;
            }
            rmsDev[ring] = Math.sqrt(sumSq / NukePattern.RING_COUNTS[ring]);
        }
        // Innermost ring should be markedly tighter than the outermost ring.
        assertTrue(rmsDev[0] < rmsDev[rmsDev.length - 1] * 0.6,
                "inner ring rms deviation " + rmsDev[0] + " not tighter than outer " + rmsDev[rmsDev.length - 1]);
        // Messiness should grow monotonically outward on average.
        for (int i = 1; i < rmsDev.length; i++) {
            assertTrue(rmsDev[i] >= rmsDev[i - 1] * 0.9,
                    "messiness should not decrease from ring " + (i - 1) + " to " + i);
        }
    }

    @Test
    void angularJitterBreaksPerfectCircles() {
        List<NukePattern.Placement> even = NukePattern.generatePlacements(new Random(7),
                new double[]{20.0}, new int[]{24}, 0.0, NukePattern.FUSE_TICKS, 0.0);
        List<NukePattern.Placement> ragged = NukePattern.generatePlacements(new Random(7),
                new double[]{20.0}, new int[]{24}, 0.0, NukePattern.FUSE_TICKS, 0.5);
        int moved = 0;
        for (int i = 0; i < even.size(); i++) {
            double d = Math.hypot(even.get(i).dx() - ragged.get(i).dx(), even.get(i).dz() - ragged.get(i).dz());
            if (d > 1e-9) {
                moved++;
            }
            assertEquals(20.0, Math.hypot(ragged.get(i).dx(), ragged.get(i).dz()), 1e-6);
        }
        assertTrue(moved > even.size() / 2, "jitter should move most charges off even spacing");
    }
}
