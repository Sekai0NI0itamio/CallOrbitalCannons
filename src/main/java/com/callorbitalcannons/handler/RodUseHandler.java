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
import net.minecraft.server.network.ServerPlayerEntity;
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
 * within the configured range, and schedules the strike server-side.
 *
 * <p>The rod breaks on a queued strike (single durability point, survival
 * only unless configured otherwise). Misses and rejected strikes never
 * consume it. The Orbital rod is fully silent in chat on every outcome —
 * only the warning sound plays; the Stab rod reports in chat as before.
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
        boolean silent = mode == WeaponMode.NUKE;
        OrbitalConfigData config = OrbitalConfig.get();
        HitResult hit = player.raycast(config.targetingRange, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            tell(player, silent, Text.literal("No target in range (" + (int) config.targetingRange + " blocks).")
                    .formatted(Formatting.GRAY));
            return TypedActionResult.fail(stack);
        }
        BlockPos target = ((BlockHitResult) hit).getBlockPos();
        if (!StrikeValidator.withinRange(player.getX(), player.getEyeY(), player.getZ(),
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, config.targetingRange + 1.0)) {
            tell(player, silent, Text.literal("Target out of range.").formatted(Formatting.GRAY));
            return TypedActionResult.fail(stack);
        }
        ServerWorld serverWorld = (ServerWorld) world;
        StrikeScheduler.ScheduleResult result =
                scheduler.schedule(serverWorld, target, mode, player.getUuid(), config);
        switch (result) {
            case QUEUED -> {
                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1.0f, 1.0f);
                if (!silent) {
                    player.sendMessage(Text.literal("Stab shot inbound at ")
                            .formatted(Formatting.GOLD)
                            .append(Text.literal(target.toShortString()).formatted(Formatting.WHITE)), false);
                }
                breakRod(player, serverWorld, stack, config);
                return TypedActionResult.consume(stack);
            }
            case COOLDOWN ->
                    tell(player, silent, Text.literal("Orbital cannon recharging.").formatted(Formatting.GRAY));
            case TOO_MANY ->
                    tell(player, silent, Text.literal("Too many strikes in flight.").formatted(Formatting.RED));
            case DUPLICATE ->
                    tell(player, silent, Text.literal("Strike already inbound on that target.").formatted(Formatting.GRAY));
            case INVALID ->
                    tell(player, silent, Text.literal("Invalid target.").formatted(Formatting.RED));
        }
        return TypedActionResult.fail(stack);
    }

    private static void tell(PlayerEntity player, boolean silent, Text message) {
        if (!silent) {
            player.sendMessage(message, false);
        }
    }

    /** Breaks the single-durability rod; creative rods survive unless configured. */
    private static void breakRod(PlayerEntity player, ServerWorld world, ItemStack stack, OrbitalConfigData config) {
        if (player.isCreative() && !config.consumeInCreative) {
            return;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            stack.damage(stack.getMaxDamage(), world, serverPlayer, item -> {
            });
        }
        if (!stack.isEmpty()) {
            stack.decrement(1);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
