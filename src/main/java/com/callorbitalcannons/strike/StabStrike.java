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
 * Concentrated Stab Shot: groups of 5 overlapping-fuse TNT charges stepped
 * down in 4-block increments from 5 above the target, each with a 1-tick
 * fuse and no gravity, stopping after the first blast-resistant block.
 */
public final class StabStrike {
    private StabStrike() {
    }

    /** Spawns the column; returns the number of charges spawned. */
    public static int spawn(ServerWorld world, BlockPos target, OrbitalConfigData config) {
        int startY = target.getY() + StabPattern.START_OFFSET_ABOVE;
        int minY = world.getBottomY();
        List<Integer> steps = StabPattern.columnSteps(startY, minY, config.stabStepSize, config.stabMaxSteps);
        List<Double> offsets = StabPattern.stepOffsets(config.stabTntPerStep, config.stabVerticalOffset);

        int spawned = 0;
        int groups = 0;
        for (int y : steps) {
            BlockPos probe = new BlockPos(target.getX(), y, target.getZ());
            float resistance = world.getBlockState(probe).getBlock().getBlastResistance();
            boolean stopping = StabPattern.isStoppingBlock(resistance, config.blastResistanceStop);
            for (double offset : offsets) {
                TntEntity tnt = new TntEntity(world, target.getX() + 0.5, y + offset, target.getZ() + 0.5, null);
                tnt.setFuse(config.stabFuseTicks);
                tnt.setNoGravity(true);
                tnt.setVelocity(Vec3d.ZERO);
                if (world.spawnEntity(tnt)) {
                    spawned++;
                }
            }
            groups++;
            if (stopping) {
                OrbitalConfig.LOG.info("[CallOrbitalCannons] stab stopped by blast-resistant block at {}.", probe.toShortString());
                break;
            }
        }
        OrbitalConfig.LOG.info("[CallOrbitalCannons] stab strike at {} spawned {} charges in {} groups.",
                target.toShortString(), spawned, groups);
        return spawned;
    }
}
