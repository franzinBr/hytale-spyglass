/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.ZoomManager;
import dev.franzin.spyglass.interaction.SpyglassZoomInteraction;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpyglassTickSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query;
    private final Map<String, Optional<ZoomManager.ZoomSettings>> zoomSettingsByItemId = new ConcurrentHashMap<>();

    public SpyglassTickSystem() {
        this.query = Query.and(Player.getComponentType());
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Player player = EntityUtils.toHolder(index, archetypeChunk).getComponent(Player.getComponentType());
        if (player == null) {
            return;
        }

        @SuppressWarnings("removal")
        UUID playerId = player.getUuid();
        assert playerId != null;

        ZoomManager zoomManager = ZoomManager.getInstance();

        if (!zoomManager.isZooming(playerId)) {
            return;
        }

        Optional<ZoomManager.ZoomSettings> equippedSettings = getEquippedZoomSettings(player);
        if (equippedSettings.isEmpty()) {
            zoomManager.disableZoom(playerId);
            return;
        }
        if (!zoomManager.isZoomConfigMatching(playerId, equippedSettings.get())) {
            zoomManager.disableZoom(playerId);
            return;
        }

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        World world = commandBuffer.getExternalData().getWorld();

        zoomManager.updateZoom(playerId, commandBuffer, world, playerRef);
    }

    @Nonnull
    public Optional<ZoomManager.ZoomSettings> getEquippedZoomSettings(Player player) {
        var itemInHand = player.getInventory().getItemInHand();
        if (itemInHand == null || itemInHand.getItem() == null) {
            return Optional.empty();
        }

        String itemId = itemInHand.getItemId();
        var interactions = itemInHand.getItem().getInteractions();
        if (interactions == null || interactions.isEmpty()) {
            return Optional.empty();
        }

        if (itemId == null || itemId.isBlank()) {
            return findSpyglassZoomSettings(interactions);
        }

        return zoomSettingsByItemId.computeIfAbsent(itemId, ignored -> findSpyglassZoomSettings(interactions));
    }

    public void clearInteractionCache() {
        zoomSettingsByItemId.clear();
    }

    private Optional<ZoomManager.ZoomSettings> findSpyglassZoomSettings(Map<?, String> interactions) {
        for (String rootInteractionId : interactions.values()) {
            RootInteraction root = RootInteraction.getAssetMap().getAsset(rootInteractionId);
            if (root == null || root.getInteractionIds() == null) {
                continue;
            }

            for (String interactionId : root.getInteractionIds()) {
                Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
                if (interaction instanceof SpyglassZoomInteraction zoomInteraction) {
                    return Optional.of(ZoomManager.ZoomSettings.of(
                            zoomInteraction.getMaxDistance(),
                            zoomInteraction.getMinDistance(),
                            zoomInteraction.getDefaultZoomMultiplier(),
                            zoomInteraction.getMaxZoomMultiplier(),
                            zoomInteraction.getZoomMultiplierStep(),
                            zoomInteraction.getOverlayTexturePath()
                    ));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) {
        return false;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
