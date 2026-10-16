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
 * Stab Shot: the full vertical column at the target's x/z, swept top-down
 * from the world ceiling to the floor in groups of overlapping-fuse TNT
 * charges (3 per group every 8 blocks by default; see config). Each charge
 * has a 1-tick fuse and no gravity. The drill stops after spawning on the
 * first blast-resistant block (resistance &ge; threshold), like the
 * reference stopping rule — so a resistant surface (obsidian platform,
 * Nether ceiling, bedrock floor) ends the drill there and nothing below
 * that layer is hit.
 */
public final class StabStrike {
    private StabStrike() {
    }

    /** Spawns the column; returns the number of charges spawned. */
    public static int spawn(ServerWorld world, BlockPos target, OrbitalConfigData config) {
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;
        List<Integer> steps =
                StabPattern.fullColumnStepsDown(maxY, minY, config.stabStepSize, config.stabMaxSteps);
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
                OrbitalConfig.LOG.info("[CallOrbitalCannons] stab stopped by blast-resistant block at {}.",
                        probe.toShortString());
                break;
            }
        }
        OrbitalConfig.LOG.info("[CallOrbitalCannons] stab strike at x={} z={} spawned {} charges in {} groups.",
                target.getX(), target.getZ(), spawned, groups);
        return spawned;
    }
}
