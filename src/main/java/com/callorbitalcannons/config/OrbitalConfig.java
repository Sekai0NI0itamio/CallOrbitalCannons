package com.callorbitalcannons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads {@code config/callorbitalcannons.json}, creating defaults when the
 * file is missing and quarantining malformed files as
 * {@code callorbitalcannons.json.invalid} instead of overwriting them.
 */
public final class OrbitalConfig {
    public static final String MOD_ID = "callorbitalcannons";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static OrbitalConfigData instance = new OrbitalConfigData();

    private OrbitalConfig() {
    }

    public static OrbitalConfigData get() {
        return instance;
    }

    /** Test seam: replaces the live config without touching disk. */
    static void setForTests(OrbitalConfigData data) {
        instance = data;
    }

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("callorbitalcannons.json");
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            instance = new OrbitalConfigData();
            save();
            return;
        }
        try {
            String json = Files.readString(path);
            OrbitalConfigData parsed = GSON.fromJson(json, OrbitalConfigData.class);
            if (parsed == null) {
                throw new JsonParseException("config parsed to null");
            }
            instance = parsed;
            for (String warning : instance.validate()) {
                LOG.warn("[CallOrbitalCannons] config: {}", warning);
            }
        } catch (IOException | JsonParseException | RuntimeException e) {
            LOG.error("[CallOrbitalCannons] Failed to load config, keeping safe defaults", e);
            try {
                Files.move(path, path.resolveSibling("callorbitalcannons.json.invalid"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveError) {
                LOG.error("[CallOrbitalCannons] Could not quarantine malformed config", moveError);
            }
            instance = new OrbitalConfigData();
        }
    }

    public static void save() {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(instance));
        } catch (IOException e) {
            LOG.error("[CallOrbitalCannons] Failed to save config", e);
        }
    }

    public static void reload() {
        load();
    }
}
