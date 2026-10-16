package com.callorbitalcannons.strike;

import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.geometry.StabPattern;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Stab Shot: the full vertical column at the target's x/z, from the world
 * bottom to the top, swept bottom-up in groups of 5 overlapping-fuse TNT
 * charges every 4 blocks. Each charge has a 1-tick fuse and no gravity.
 */
public final class StabStrike {
    private StabStrike() {
    }

    /** Spawns the column; returns the number of charges spawned. */
    public static int spawn(ServerWorld world, BlockPos target, OrbitalConfigData config) {
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;
        List<Integer> steps =
                StabPattern.fullColumnStepsUp(minY, maxY, config.stabStepSize, config.stabMaxSteps);
        List<Double> offsets = StabPattern.stepOffsets(config.stabTntPerStep, config.stabVerticalOffset);

        int spawned = 0;
        for (int y : steps) {
            for (double offset : offsets) {
                TntEntity tnt = new TntEntity(world, target.getX() + 0.5, y + offset, target.getZ() + 0.5, null);
                tnt.setFuse(config.stabFuseTicks);
                tnt.setNoGravity(true);
                tnt.setVelocity(Vec3d.ZERO);
                if (world.spawnEntity(tnt)) {
                    spawned++;
                }
            }
        }
        OrbitalConfig.LOG.info("[CallOrbitalCannons] stab strike at x={} z={} spawned {} charges in {} groups.",
                target.getX(), target.getZ(), spawned, steps.size());
        return spawned;
    }
}
