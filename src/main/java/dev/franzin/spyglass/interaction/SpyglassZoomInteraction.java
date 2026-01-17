/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
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

    public static final BuilderCodec<SpyglassZoomInteraction> CODEC =
            ((BuilderCodec.Builder) BuilderCodec.builder(
                    SpyglassZoomInteraction.class,
                    SpyglassZoomInteraction::new,
                    SimpleInstantInteraction.CODEC
            )).build();

    public SpyglassZoomInteraction(String id) {
        super(id);
    }

    protected SpyglassZoomInteraction() {}

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


        ZoomManager.getInstance().toggleZoom(playerId, player, playerRef);
    }
}