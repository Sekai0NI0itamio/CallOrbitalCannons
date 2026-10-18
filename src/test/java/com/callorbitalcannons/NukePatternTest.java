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
    void ringGapsAreUniform() {
        for (int i = 1; i < NukePattern.RING_RADII.length; i++) {
            assertEquals(5.0, NukePattern.RING_RADII[i] - NukePattern.RING_RADII[i - 1], 1e-6);
        }
    }

    @Test
    void ringCountsMatchMk6Baseline() {
        // cubicmetre Mk.6: the amount of TNT added to a ring is proportional
        // to its radius, round(2.5 × radius), but front-loaded — the first
        // three rings (24, 48, 56) carry the dense opening impact, then it
        // continues 52 → 128 outward. 759 ring charges + 1 center = 760 TNT.
        assertArrayEquals(new int[]{24, 48, 56, 52, 65, 78, 90, 103, 115, 128},
                NukePattern.RING_COUNTS);
    }

    @Test
    void ringTotalsMatchMk6Baseline() {
        assertEquals(759, NukePattern.ringTotal());
        assertEquals(760, NukePattern.totalPayload());
    }

    @Test
    void innerRingsAreDenseForOpeningImpact() {
        // The 2nd ring should pack tight for the dense opening impact.
        int[] counts = NukePattern.RING_COUNTS;
        assertTrue(counts[1] > counts[0],
                "2nd ring (" + counts[1] + ") denser than 1st (" + counts[0] + ")");
        assertTrue(counts[2] > counts[1],
                "3rd ring (" + counts[2] + ") denser than 2nd (" + counts[1] + ")");
        assertTrue(counts[counts.length - 1] > counts[counts.length - 2],
                "outer ring still carries the heaviest ring");
    }

    @Test
    void chargesAreNotEvenlySpacedAroundRings() {
        // Mk.6 places charges at random angles around each ring: gaps
        // between consecutive charges vary widely, so rings render as dense
        // clumps rather than perfectly even dots.
        Random random = new Random(42);
        List<NukePattern.Placement> placements = NukePattern.generatePlacements(random,
                NukePattern.RING_RADII, NukePattern.RING_COUNTS,
                0.0, NukePattern.FUSE_TICKS, 1.0);
        int[] counts = NukePattern.RING_COUNTS;
        int offset = 0;
        for (int ring = 0; ring < counts.length; ring++) {
            double spacing = 2.0 * Math.PI / counts[ring];
            double[] angles = new double[counts[ring]];
            for (int i = 0; i < counts[ring]; i++) {
                NukePattern.Placement p = placements.get(offset + i);
                angles[i] = Math.atan2(p.dz(), p.dx());
            }
            java.util.Arrays.sort(angles);
            double minGap = Double.MAX_VALUE;
            double maxGap = 0.0;
            for (int i = 1; i < angles.length; i++) {
                double gap = angles[i] - angles[i - 1];
                minGap = Math.min(minGap, gap);
                maxGap = Math.max(maxGap, gap);
            }
            double wrap = 2.0 * Math.PI - angles[angles.length - 1] + angles[0];
            minGap = Math.min(minGap, wrap);
            maxGap = Math.max(maxGap, wrap);
            assertTrue(maxGap - minGap > spacing * 0.5,
                    "ring " + ring + " gaps too uniform [" + minGap + ", " + maxGap + "] for Mk.6 random placement");
            offset += counts[ring];
        }
    }

    @Test
    void defaultsMatchMk6Baseline() {
        assertEquals(72.0, NukePattern.SPAWN_HEIGHT, 1e-9);
        assertEquals(79, NukePattern.FUSE_TICKS);
        assertEquals(0.2, NukePattern.MISALIGN_RANGE, 1e-9);
        assertEquals(1.0, NukePattern.ANGULAR_JITTER, 1e-9);
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
    void zeroScatterKeepsChargesOnRingRadius() {
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
        // Every ring stays clearly circular: the flat 0.2 disc keeps radial
        // deviation far under half a block on all rings alike.
        for (int ring = 0; ring < rmsDev.length; ring++) {
            assertTrue(rmsDev[ring] < 0.5,
                    "ring " + ring + " rms deviation " + rmsDev[ring] + " too high, not circular");
        }
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
