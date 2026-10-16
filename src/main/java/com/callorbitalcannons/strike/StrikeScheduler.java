package com.callorbitalcannons.strike;

import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.geometry.CooldownTracker;
import com.callorbitalcannons.geometry.StrikeValidator;
import com.callorbitalcannons.item.WeaponMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Authoritative server-side strike queue. Activations only schedule a
 * request; entities are spawned later on the server thread by
 * {@link #tick(MinecraftServer)}, which revalidates the world, the chunk,
 * and the coordinates at fire time.
 */
public final class StrikeScheduler {
    public enum ScheduleResult {
        QUEUED,
        COOLDOWN,
        TOO_MANY,
        INVALID,
        DUPLICATE
    }

    private record Request(ServerWorld world, BlockPos pos, WeaponMode mode, UUID caster, long fireAt) {
    }

    private final List<Request> pending = new ArrayList<>();
    private final CooldownTracker cooldowns = new CooldownTracker();
    private final Random random = new Random();

    /**
     * Schedules {@code mode} at {@code pos} after the configured delay.
     * {@code caster} may be null (command / dispenser use): cooldown and
     * duplicate tracking are then skipped.
     */
    public synchronized ScheduleResult schedule(ServerWorld world, BlockPos pos, WeaponMode mode,
                                                UUID caster, OrbitalConfigData config) {
        if (world == null || pos == null || mode == null) {
            return ScheduleResult.INVALID;
        }
        if (!StrikeValidator.saneTarget(pos.getX(), pos.getY(), pos.getZ(),
                world.getBottomY(), world.getTopY() - 1)) {
            OrbitalConfig.LOG.warn("[CallOrbitalCannons] rejected strike: insane target {}.", pos.toShortString());
            return ScheduleResult.INVALID;
        }
        if (pending.size() >= config.maxActiveStrikes) {
            OrbitalConfig.LOG.warn("[CallOrbitalCannons] rejected strike: {} strikes already pending (max {}).",
                    pending.size(), config.maxActiveStrikes);
            return ScheduleResult.TOO_MANY;
        }
        long now = world.getServer().getTicks();
        if (caster != null) {
            if (!cooldowns.tryFire(caster, now, config.playerCooldownTicks)) {
                return ScheduleResult.COOLDOWN;
            }
            for (Request request : pending) {
                if (mode == request.mode() && pos.equals(request.pos()) && caster.equals(request.caster())) {
                    return ScheduleResult.DUPLICATE;
                }
            }
        }
        pending.add(new Request(world, pos.toImmutable(), mode, caster, now + config.strikeDelayTicks));
        return ScheduleResult.QUEUED;
    }

    /** Fires every due strike. Must run on the server thread. */
    public synchronized void tick(MinecraftServer server) {
        if (pending.isEmpty()) {
            return;
        }
        long now = server.getTicks();
        List<Request> due = new ArrayList<>();
        for (Request request : pending) {
            if (request.fireAt() <= now) {
                due.add(request);
            }
        }
        pending.removeAll(due);
        OrbitalConfigData config = OrbitalConfig.get();
        for (Request request : due) {
            fire(request, config);
        }
    }

    private void fire(Request request, OrbitalConfigData config) {
        ServerWorld world = request.world();
        BlockPos pos = request.pos();
        if (!world.isChunkLoaded(pos)) {
            OrbitalConfig.LOG.warn("[CallOrbitalCannons] dropped {} strike: chunk unloaded at {}.",
                    request.mode().id(), pos.toShortString());
            return;
        }
        if (request.mode() == WeaponMode.NUKE) {
            NukeStrike.spawn(world, pos, config, random);
        } else {
            StabStrike.spawn(world, pos, config);
        }
    }

    /** Drops queued strikes in {@code world} (chunk/level unload). */
    public synchronized int prune(ServerWorld world) {
        int before = pending.size();
        pending.removeIf(request -> request.world() == world);
        return before - pending.size();
    }

    /** Drops everything (server shutdown). Returns the dropped count. */
    public synchronized int clear() {
        int count = pending.size();
        pending.clear();
        cooldowns.clear();
        return count;
    }

    public synchronized int pendingCount() {
        return pending.size();
    }
}
