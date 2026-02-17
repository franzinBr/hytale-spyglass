/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */


package dev.franzin.spyglass;

import com.hypixel.hytale.assetstore.event.GenerateAssetsEvent;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.franzin.spyglass.interaction.SpyglassStepZoomInteraction;
import dev.franzin.spyglass.interaction.SpyglassZoomInteraction;
import dev.franzin.spyglass.system.SpyglassTickSystem;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Spyglass extends JavaPlugin {

    private static Spyglass instance;

    public static final String NAMESPACE = "Spyglass";
    public static final String ZOOM_INTERACTION_ID = "Spyglass_Zoom";
    public static final String STEP_ZOOM_INTERACTION_ID = "Spyglass_Step_Zoom";

    private SpyglassTickSystem spyglassTickSystem;

    public Spyglass(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
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


        this.spyglassTickSystem = new SpyglassTickSystem();
        getEntityStoreRegistry().registerSystem(this.spyglassTickSystem);

        getEventRegistry().registerGlobal(
                PlayerDisconnectEvent.class,
                event -> ZoomManager.getInstance().disableZoom(event.getPlayerRef().getUuid())
        );

        registerAssetCacheInvalidationEvents();

        log("Plugin setup complete!");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerAssetCacheInvalidationEvents() {
        getEventRegistry().registerGlobal(
                (Class) LoadedAssetsEvent.class,
                event -> this.spyglassTickSystem.clearInteractionCache()
        );
        getEventRegistry().registerGlobal(
                (Class) RemovedAssetsEvent.class,
                event -> this.spyglassTickSystem.clearInteractionCache()
        );
        getEventRegistry().registerGlobal(
                (Class) GenerateAssetsEvent.class,
                event -> this.spyglassTickSystem.clearInteractionCache()
        );
    }

    @Override
    protected void start() {
        log("Plugin enabled!");
    }

    @Override
    public void shutdown() {
        log("Plugin disabled!");
    }

    private void log(@Nonnull String message) {
        getLogger().at(Level.INFO).log("[" + NAMESPACE + "] " + message);
    }

    public static Spyglass getInstance() {
        return instance;
    }
}
