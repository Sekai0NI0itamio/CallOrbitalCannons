package com.callorbitalcannons.handler;

import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.config.OrbitalConfigData;
import com.callorbitalcannons.geometry.StrikeValidator;
import com.callorbitalcannons.item.OrbitalRod;
import com.callorbitalcannons.item.WeaponMode;
import com.callorbitalcannons.strike.StrikeScheduler;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Right-click targeting: validates the weapon, raycasts to the aimed block
 * within the configured range, and schedules the strike server-side with
 * audiovisual feedback. The rod is consumed exactly once per queued strike;
 * misses, cooldowns, and rejected strikes never consume it.
 */
public final class RodUseHandler {
    private RodUseHandler() {
    }

    public static void register(StrikeScheduler scheduler) {
        UseItemCallback.EVENT.register((player, world, hand) ->
                activate(player, world, player.getStackInHand(hand), scheduler));
    }

    static TypedActionResult<ItemStack> activate(PlayerEntity player,
                                                 net.minecraft.world.World world,
                                                 ItemStack stack,
                                                 StrikeScheduler scheduler) {
        WeaponMode mode = OrbitalRod.modeOf(stack);
        if (mode == null) {
            return TypedActionResult.pass(stack);
        }
        if (world.isClient()) {
            return TypedActionResult.success(stack);
        }
        OrbitalConfigData config = OrbitalConfig.get();
        HitResult hit = player.raycast(config.targetingRange, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.sendMessage(Text.literal("No target in range (" + (int) config.targetingRange + " blocks).")
                    .formatted(Formatting.GRAY), false);
            return TypedActionResult.fail(stack);
        }
        BlockPos target = ((BlockHitResult) hit).getBlockPos();
        if (!StrikeValidator.withinRange(player.getX(), player.getEyeY(), player.getZ(),
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, config.targetingRange + 1.0)) {
            player.sendMessage(Text.literal("Target out of range.").formatted(Formatting.GRAY), false);
            return TypedActionResult.fail(stack);
        }
        ServerWorld serverWorld = (ServerWorld) world;
        StrikeScheduler.ScheduleResult result =
                scheduler.schedule(serverWorld, target, mode, player.getUuid(), config);
        switch (result) {
            case QUEUED -> {
                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.sendMessage(Text.literal(mode == WeaponMode.NUKE
                                ? "Orbital strike inbound at "
                                : "Stab shot inbound at ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal(target.toShortString()).formatted(Formatting.WHITE)), false);
                if (!player.isCreative() || config.consumeInCreative) {
                    stack.decrement(1);
                }
                return TypedActionResult.consume(stack);
            }
            case COOLDOWN ->
                    player.sendMessage(Text.literal("Orbital cannon recharging.").formatted(Formatting.GRAY), false);
            case TOO_MANY ->
                    player.sendMessage(Text.literal("Too many strikes in flight.").formatted(Formatting.RED), false);
            case DUPLICATE ->
                    player.sendMessage(Text.literal("Strike already inbound on that target.").formatted(Formatting.GRAY), false);
            case INVALID ->
                    player.sendMessage(Text.literal("Invalid target.").formatted(Formatting.RED), false);
        }
        return TypedActionResult.fail(stack);
    }
}
