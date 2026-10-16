package com.callorbitalcannons;

import com.callorbitalcannons.geometry.TargetRay;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetRayTest {
    private static TargetRay.ChunkView loadedBox(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return (x, y, z) -> x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    @Test
    void cleanMissInsideLoadedChunksIsEmpty() {
        TargetRay.ChunkView everywhere = (x, y, z) -> true;
        Optional<TargetRay.Target> result = TargetRay.approximate(
                0.5, 65.0, 0.5, 0.0, 1.0, 0.0, 200.0, -64, 319, everywhere);
        assertTrue(result.isEmpty(), "sky aim inside loaded chunks must keep the rod");
    }

    @Test
    void rayLeavingLoadedAreaApproximatesLastLoadedVoxel() {
        TargetRay.ChunkView loaded = loadedBox(-100, 100, -64, 319, -100, 100);
        Optional<TargetRay.Target> result = TargetRay.approximate(
                0.5, 65.0, 0.5, 1.0, 0.0, 0.0, 200.0, -64, 319, loaded);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().x());
        assertEquals(65, result.get().y());
        assertEquals(0, result.get().z());
        assertTrue(result.get().approximate());
    }

    @Test
    void approximateYIsClampedIntoWorld() {
        // Chunk loading ignores Y, so a steep upward ray can leave the world
        // while staying "loaded": the fallback Y must clamp to the bounds.
        TargetRay.ChunkView loaded = loadedBox(-100, 100, -64, 400, -100, 100);
        Optional<TargetRay.Target> result = TargetRay.approximate(
                0.5, 300.0, 0.5, 0.3, 1.0, 0.0, 200.0, -64, 319, loaded);
        assertTrue(result.isPresent());
        assertEquals(319, result.get().y());
    }

    @Test
    void zeroDirectionOrRangeIsEmpty() {
        TargetRay.ChunkView everywhere = (x, y, z) -> true;
        assertTrue(TargetRay.approximate(0.5, 65.0, 0.5, 0.0, 0.0, 0.0, 200.0, -64, 319, everywhere).isEmpty());
        assertTrue(TargetRay.approximate(0.5, 65.0, 0.5, 1.0, 0.0, 0.0, 0.0, -64, 319, everywhere).isEmpty());
    }

    @Test
    void diagonalRayApproximatesBoundaryVoxel() {
        TargetRay.ChunkView loaded = loadedBox(-50, 50, -64, 319, -50, 50);
        Optional<TargetRay.Target> result = TargetRay.approximate(
                0.5, 65.0, 0.5, 1.0, 0.0, 1.0, 200.0, -64, 319, loaded);
        assertTrue(result.isPresent());
        assertTrue(result.get().x() <= 50 && result.get().z() <= 50);
        assertTrue(result.get().x() >= 49 && result.get().z() >= 49);
    }
}
