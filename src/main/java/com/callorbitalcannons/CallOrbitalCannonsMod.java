package com.callorbitalcannons;

import com.callorbitalcannons.command.OrbitalCommand;
import com.callorbitalcannons.config.OrbitalConfig;
import com.callorbitalcannons.handler.RodUseHandler;
import com.callorbitalcannons.recipe.WeaponRodRecipe;
import com.callorbitalcannons.strike.StrikeScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Server-authoritative entrypoint: config, weapon-rod recipes, the
 * {@code /orbital} command tree, rod activation, and the strike tick loop.
 * All strikes spawn on the server thread; clients only see vanilla TNT
 * entities and explosions.
 */
public class CallOrbitalCannonsMod implements ModInitializer {
    public static final String MOD_ID = "callorbitalcannons";

    private final StrikeScheduler scheduler = new StrikeScheduler();

    @Override
    public void onInitialize() {
        OrbitalConfig.load();

        Registry.register(Registries.RECIPE_SERIALIZER,
                Identifier.of(MOD_ID, "orbital_rod"), WeaponRodRecipe.Serializers.ORBITAL);
        Registry.register(Registries.RECIPE_SERIALIZER,
                Identifier.of(MOD_ID, "stab_rod"), WeaponRodRecipe.Serializers.STAB);

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                OrbitalCommand.register(dispatcher, access, environment, scheduler));

        ServerTickEvents.END_SERVER_TICK.register(scheduler::tick);
        RodUseHandler.register(scheduler);

        ServerWorldEvents.UNLOAD.register((server, world) -> scheduler.prune(world));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            int dropped = scheduler.clear();
            if (dropped > 0) {
                OrbitalConfig.LOG.info("[CallOrbitalCannons] cleared {} pending strikes on shutdown.", dropped);
            }
        });

        OrbitalConfig.LOG.info("[CallOrbitalCannons] initialized.");
    }
}
