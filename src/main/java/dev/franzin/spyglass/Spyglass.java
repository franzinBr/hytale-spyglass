/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */


package dev.franzin.spyglass;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.franzin.spyglass.config.ConfigValidator;
import dev.franzin.spyglass.config.SpyglassConfig;
import dev.franzin.spyglass.config.ZoomSettings;
import dev.franzin.spyglass.interaction.SpyglassStepZoomInteraction;
import dev.franzin.spyglass.interaction.SpyglassZoomInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.franzin.spyglass.system.SpyglassActiveSlotChangedSystem;
import dev.franzin.spyglass.system.SpyglassHotbarChangedSystem;
import dev.franzin.spyglass.system.SpyglassRespawnSystem;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Path;

public class Spyglass extends JavaPlugin {

    private static Spyglass instance;
    private final Config<SpyglassConfig> config;
    private ZoomManager zoomManager;

    public static final String NAMESPACE = "Spyglass";
    public static final String SPYGLASS_ITEM_ID = "Spyglass";
    public static final String ZOOM_INTERACTION_ID = "Spyglass_Zoom";
    public static final String STEP_ZOOM_INTERACTION_ID = "Spyglass_Step_Zoom";

    public Spyglass(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        config = withConfig(SpyglassConfig.CODEC);
    }

    @Override
    protected void setup() {
        Path configPath = getDataDirectory().resolve("config.json");
        boolean firstStartup = Files.notExists(configPath);
        ZoomSettings settings = ConfigValidator.validateAndSnapshot(config.get());
        if (firstStartup) config.save().join();
        zoomManager = new ZoomManager(this, settings);
        log("Loaded Spyglass zoom config: " + settings.magnificationLevels().length + " levels, FOV "
                + settings.minimumFov() + ".." + settings.maximumFov() + ", transition "
                + settings.transitionDurationMillis() + " ms");
        log("Registering plugin...");

        this.getCodecRegistry(Interaction.CODEC).register(
                ZOOM_INTERACTION_ID,
                SpyglassZoomInteraction.class,
                SpyglassZoomInteraction.CODEC
        );

        this.getCodecRegistry(Interaction.CODEC).register(
                STEP_ZOOM_INTERACTION_ID,
                SpyglassStepZoomInteraction.class,
                SpyglassStepZoomInteraction.CODEC
        );


        getEntityStoreRegistry().registerSystem(new SpyglassActiveSlotChangedSystem(zoomManager));
        getEntityStoreRegistry().registerSystem(new SpyglassHotbarChangedSystem(zoomManager));
        getEntityStoreRegistry().registerSystem(new SpyglassRespawnSystem(zoomManager));

        getEventRegistry().registerGlobal(
                PlayerDisconnectEvent.class,
                event -> zoomManager.disableZoom(event.getPlayerRef().getUuid(), "player disconnected")
        );

        getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, event -> {
            PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                zoomManager.disableZoom(playerRef.getUuid(), "player changed world");
            }
        });

        log("Plugin setup complete!");
    }

    @Override
    protected void start() {
        log("Plugin enabled!");
    }

    @Override
    public void shutdown() {
        if (zoomManager != null) zoomManager.disableAll();
        zoomManager = null;
        instance = null;
        log("Plugin disabled!");
    }

    private void log(@Nonnull String message) {
        getLogger().at(Level.INFO).log("[" + NAMESPACE + "] " + message);
    }

    public static Spyglass getInstance() {
        return instance;
    }

    public ZoomManager getZoomManager() {
        if (zoomManager == null) throw new IllegalStateException("Spyglass has not completed setup");
        return zoomManager;
    }
}
