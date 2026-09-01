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
import dev.franzin.spyglass.interaction.SpyglassStepZoomInteraction;
import dev.franzin.spyglass.interaction.SpyglassZoomInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.franzin.spyglass.system.SpyglassActiveSlotChangedSystem;
import dev.franzin.spyglass.system.SpyglassHotbarChangedSystem;
import dev.franzin.spyglass.system.SpyglassRespawnSystem;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Spyglass extends JavaPlugin {

    private static Spyglass instance;

    public static final String NAMESPACE = "Spyglass";
    public static final String SPYGLASS_ITEM_ID = "Spyglass";
    public static final String ZOOM_INTERACTION_ID = "Spyglass_Zoom";
    public static final String STEP_ZOOM_INTERACTION_ID = "Spyglass_Step_Zoom";

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


        getEntityStoreRegistry().registerSystem(new SpyglassActiveSlotChangedSystem());
        getEntityStoreRegistry().registerSystem(new SpyglassHotbarChangedSystem());
        getEntityStoreRegistry().registerSystem(new SpyglassRespawnSystem());

        getEventRegistry().registerGlobal(
                PlayerDisconnectEvent.class,
                event -> ZoomManager.getInstance().disableZoom(event.getPlayerRef().getUuid(), "player disconnected")
        );

        getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, event -> {
            PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                ZoomManager.getInstance().disableZoom(playerRef.getUuid(), "player changed world");
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
        ZoomManager.getInstance().disableAll();
        log("Plugin disabled!");
    }

    private void log(@Nonnull String message) {
        getLogger().at(Level.INFO).log("[" + NAMESPACE + "] " + message);
    }

    public static Spyglass getInstance() {
        return instance;
    }
}
