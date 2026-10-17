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
        assertArrayEquals(new double[]{6.0, 13.0, 20.0, 27.0, 34.0, 41.0, 48.0},
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
        assertArrayEquals(new int[]{8, 16, 24, 32, 40, 48, 56}, NukePattern.RING_COUNTS);
    }

    @Test
    void ringTotalsMatchTwoTwentyFiveBaseline() {
        assertEquals(224, NukePattern.ringTotal());
        assertEquals(225, NukePattern.totalPayload());
    }

    @Test
    void ringCountsGrowByEightOutward() {
        int[] counts = NukePattern.RING_COUNTS;
        for (int i = 0; i < counts.length; i++) {
            assertEquals(8 * (i + 1), counts[i], "ring " + i + " should have 8*(i+1) charges");
        }
        // Charge spacing grows progressively wider outward (circumference
        // outruns the +8-per-ring charge growth), matching the
        // pointillist outer rings described in the reference footage.
        double[] radii = NukePattern.RING_RADII;
        double prevSpacing = -1.0;
        for (int i = 0; i < counts.length; i++) {
            double spacing = 2.0 * Math.PI * radii[i] / counts[i];
            assertTrue(spacing >= prevSpacing - 1e-9,
                    "ring " + i + " spacing " + spacing + " should not narrow vs previous " + prevSpacing);
            prevSpacing = spacing;
        }
    }

    @Test
    void defaultsMatchTunedBaseline() {
        assertEquals(72.0, NukePattern.SPAWN_HEIGHT, 1e-9);
        assertEquals(79, NukePattern.FUSE_TICKS);
        assertEquals(1.0, NukePattern.MISALIGN_RANGE, 1e-9);
        assertEquals(0.2, NukePattern.ANGULAR_JITTER, 1e-9);
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
    void allRingsStayMostlyCircular() {
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
        // Every ring should remain clearly circular: radial jitter well under 1 block.
        for (int ring = 0; ring < rmsDev.length; ring++) {
            assertTrue(rmsDev[ring] < 1.0,
                    "ring " + ring + " rms deviation " + rmsDev[ring] + " too high, not circular");
        }
        // Outer fringe may be slightly looser but not dramatically so (~70% circular target).
        assertTrue(rmsDev[rmsDev.length - 1] < rmsDev[0] * 5.0 + 0.5,
                "outer ring rms deviation " + rmsDev[rmsDev.length - 1]
                        + " dramatically messier than inner " + rmsDev[0]);
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
