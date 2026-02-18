package dev.franzin.spyglass.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.ZoomManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpyglassZoomOutInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<SpyglassZoomOutInteraction> CODEC = BuilderCodec.builder(
                    SpyglassZoomOutInteraction.class,
                    SpyglassZoomOutInteraction::new,
                    SimpleInstantInteraction.CODEC
            )
            .documentation("Decrease zoom while Spyglass zoom mode is active")
            .build();

    @Override
    protected void firstRun(@NotNull InteractionType interactionType, @NotNull InteractionContext context, @NotNull CooldownHandler cooldownHandler) {
        Ref<EntityStore> entityRef = context.getEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        UUIDComponent component = commandBuffer.getComponent(entityRef, UUIDComponent.getComponentType());
        assert component != null;
        UUID playerId = component.getUuid();

        if (!ZoomManager.getInstance().isZooming(playerId)) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ZoomManager.getInstance().zoomOut(playerId);
    }
}

