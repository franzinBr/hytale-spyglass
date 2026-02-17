/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.ZoomManager;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SpyglassZoomInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<SpyglassZoomInteraction> CODEC = BuilderCodec.builder(
                    SpyglassZoomInteraction.class,
                    SpyglassZoomInteraction::new,
                    SimpleInstantInteraction.CODEC
            )
            .appendInherited(
                    new KeyedCodec<>("OverlayTexturePath", Codec.STRING),
                    (interaction, value) -> interaction.overlayTexturePath = value,
                    interaction -> interaction.overlayTexturePath,
                    (interaction, parent) -> interaction.overlayTexturePath = parent.overlayTexturePath
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("MaxDistance", Codec.FLOAT),
                    (interaction, value) -> interaction.maxDistance = value,
                    interaction -> interaction.maxDistance,
                    (interaction, parent) -> interaction.maxDistance = parent.maxDistance
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("MinDistance", Codec.FLOAT),
                    (interaction, value) -> interaction.minDistance = value,
                    interaction -> interaction.minDistance,
                    (interaction, parent) -> interaction.minDistance = parent.minDistance
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("DefaultZoomMultiplier", Codec.FLOAT),
                    (interaction, value) -> interaction.defaultZoomMultiplier = value,
                    interaction -> interaction.defaultZoomMultiplier,
                    (interaction, parent) -> interaction.defaultZoomMultiplier = parent.defaultZoomMultiplier
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("MaxZoomMultiplier", Codec.FLOAT),
                    (interaction, value) -> interaction.maxZoomMultiplier = value,
                    interaction -> interaction.maxZoomMultiplier,
                    (interaction, parent) -> interaction.maxZoomMultiplier = parent.maxZoomMultiplier
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("ZoomMultiplierStep", Codec.FLOAT),
                    (interaction, value) -> interaction.zoomMultiplierStep = value,
                    interaction -> interaction.zoomMultiplierStep,
                    (interaction, parent) -> interaction.zoomMultiplierStep = parent.zoomMultiplierStep
            )
            .add()
            .documentation("Toggle zoom when right-clicked with a Spyglass in hand")
            .build();

    private String overlayTexturePath;
    private float maxDistance = ZoomManager.ZoomConfig.MAX_DISTANCE;
    private float minDistance = ZoomManager.ZoomConfig.MIN_DISTANCE;
    private float defaultZoomMultiplier = ZoomManager.ZoomConfig.DEFAULT_ZOOM_MULTIPLIER;
    private float maxZoomMultiplier = ZoomManager.ZoomConfig.MAX_ZOOM_MULTIPLIER;
    private float zoomMultiplierStep = ZoomManager.ZoomConfig.ZOOM_MULTIPLIER_STEP;

    public SpyglassZoomInteraction(String id) {
        super(id);
    }

    protected SpyglassZoomInteraction() {}

    public String getOverlayTexturePath() {
        return overlayTexturePath;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public float getDefaultZoomMultiplier() {
        return defaultZoomMultiplier;
    }

    public float getMaxZoomMultiplier() {
        return maxZoomMultiplier;
    }

    public float getZoomMultiplierStep() {
        return zoomMultiplierStep;
    }

    @Override
    protected void firstRun(
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> entityRef = context.getEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        if (entityRef == null || commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        @SuppressWarnings("removal")
        UUID playerId = player.getUuid();
        assert playerId != null;

        @SuppressWarnings("removal")
        var playerRef = player.getPlayerRef();

        ZoomManager.ZoomSettings settings = ZoomManager.ZoomSettings.of(
                maxDistance,
                minDistance,
                defaultZoomMultiplier,
                maxZoomMultiplier,
                zoomMultiplierStep,
                overlayTexturePath
        );
        ZoomManager.getInstance().toggleZoom(playerId, player, playerRef, settings);
    }
}
