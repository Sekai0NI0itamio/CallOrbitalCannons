package com.callorbitalcannons.command;

import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.geometry.StrikeValidator;
import com.callorbitalcannons.item.OrbitalRod;
import com.callorbitalcannons.item.WeaponMode;
import com.callorbitalcannons.strike.StrikeScheduler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * {@code /orbital} tree: testing/admin giveaways, direct strikes, and config
 * reload. Everything here bypasses crafting, so every branch requires
 * operator permission level 2.
 */
public final class OrbitalCommand {
    private OrbitalCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                net.minecraft.command.CommandRegistryAccess access,
                                CommandManager.RegistrationEnvironment environment,
                                StrikeScheduler scheduler) {
        dispatcher.register(CommandManager.literal("orbital")
                .then(CommandManager.literal("give")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("nuke")
                                .executes(context -> give(context, scheduler, WeaponMode.NUKE, null))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(context -> give(context, scheduler, WeaponMode.NUKE,
                                                EntityArgumentType.getPlayer(context, "player")))))
                        .then(CommandManager.literal("stab")
                                .executes(context -> give(context, scheduler, WeaponMode.STAB, null))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(context -> give(context, scheduler, WeaponMode.STAB,
                                                EntityArgumentType.getPlayer(context, "player"))))))
                .then(CommandManager.literal("strike")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("nuke")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> strike(context, scheduler, WeaponMode.NUKE))))
                        .then(CommandManager.literal("stab")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> strike(context, scheduler, WeaponMode.STAB)))))
                .then(CommandManager.literal("reload")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(OrbitalCommand::reload)));
    }

    private static int give(CommandContext<ServerCommandSource> context, StrikeScheduler scheduler,
                            WeaponMode mode, ServerPlayerEntity explicit) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity target = explicit != null ? explicit : source.getPlayerOrThrow();
        ItemStack rod = OrbitalRod.create(mode);
        boolean placed = target.getInventory().insertStack(rod);
        if (!placed || !rod.isEmpty()) {
            target.dropItem(rod, false);
        }
        source.sendFeedback(() -> Text.literal("Gave " + mode.displayName() + " to "
                + target.getGameProfile().getName()).formatted(Formatting.GREEN), true);
        return 1;
    }

    private static int strike(CommandContext<ServerCommandSource> context, StrikeScheduler scheduler,
                              WeaponMode mode) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        if (!StrikeValidator.saneTarget(pos.getX(), pos.getY(), pos.getZ(),
                world.getBottomY(), world.getTopY() - 1)) {
            source.sendError(Text.literal("Invalid strike coordinates.").formatted(Formatting.RED));
            return 0;
        }
        net.minecraft.entity.Entity entity = source.getEntity();
        java.util.UUID caster = entity instanceof ServerPlayerEntity player ? player.getUuid() : null;
        StrikeScheduler.ScheduleResult result =
                scheduler.schedule(world, pos, mode, caster, OrbitalConfig.get());
        if (result == StrikeScheduler.ScheduleResult.QUEUED) {
            source.sendFeedback(() -> Text.literal(mode.displayName() + " inbound at "
                    + pos.toShortString()).formatted(Formatting.GOLD), true);
            return 1;
        }
        source.sendError(Text.literal("Strike rejected: " + result.name().toLowerCase()).formatted(Formatting.RED));
        return 0;
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        OrbitalConfig.reload();
        context.getSource().sendFeedback(
                () -> Text.literal("CallOrbitalCannons config reloaded.").formatted(Formatting.GREEN), true);
        return 1;
    }
}
