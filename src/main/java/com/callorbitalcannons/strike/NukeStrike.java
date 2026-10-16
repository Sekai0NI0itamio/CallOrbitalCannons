package com.callorbitalcannons.strike;

import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.geometry.NukePattern;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

/**
 * Wide-area Nuke Shot: one stationary center charge plus concentric rings of
 * TNT dropped from high above with drag-compensated outward velocity, so the
 * charges spread back out to their ring radii as they fall.
 */
public final class NukeStrike {
    private NukeStrike() {
    }

    /**
     * Spawns the strike at vanilla TNT behavior. Returns the number of
     * charges spawned. Ring geometry is computed once per strike, never per
     * charge, and all world modification happens on the server thread.
     */
    public static int spawn(ServerWorld world, BlockPos target, OrbitalConfigData config, Random random) {
        int rings = Math.min(config.nukeRingCount, NukePattern.RING_RADII.length);
        double[] radii = new double[rings];
        int[] counts = new int[rings];
        for (int i = 0; i < rings; i++) {
            radii[i] = NukePattern.RING_RADII[i];
            counts[i] = NukePattern.RING_COUNTS[i];
        }

        List<NukePattern.Placement> placements = NukePattern.generatePlacements(random, radii, counts,
                config.nukeMisalignRange, config.nukeFuseTicks, config.nukeAngularJitter);

        int allowed = config.nukePayloadLimit;
        int ringToSpawn = Math.min(placements.size(), Math.max(0, allowed - 1));
        if (ringToSpawn < placements.size()) {
            OrbitalConfig.LOG.warn("[CallOrbitalCannons] nuke payload capped: {} of {} ring charges "
                    + "(limit {}). Raise nukePayloadLimit for the full pattern.",
                    ringToSpawn, placements.size(), allowed);
        }

        double spawnX = target.getX() + 0.5;
        double spawnZ = target.getZ() + 0.5;
        double spawnY = Math.min(target.getY() + 0.5 + config.nukeSpawnHeight, world.getTopY() - 1.0);

        int spawned = 0;
        TntEntity center = new TntEntity(world, spawnX, spawnY, spawnZ, null);
        center.setFuse(config.nukeFuseTicks);
        center.setVelocity(Vec3d.ZERO);
        if (world.spawnEntity(center)) {
            spawned++;
        }
        for (int i = 0; i < ringToSpawn; i++) {
            NukePattern.Placement placement = placements.get(i);
            TntEntity tnt = new TntEntity(world, spawnX, spawnY, spawnZ, null);
            tnt.setFuse(config.nukeFuseTicks);
            tnt.setVelocity(new Vec3d(placement.vx(), 0.0, placement.vz()));
            if (world.spawnEntity(tnt)) {
                spawned++;
            }
        }
        OrbitalConfig.LOG.info("[CallOrbitalCannons] nuke strike at {} spawned {} charges ({} rings).",
                target.toShortString(), spawned, rings);
        return spawned;
    }
}
