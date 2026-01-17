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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.Spyglass;
import dev.franzin.spyglass.ZoomManager;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class SpyglassTickSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query;

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

        if (!isSpyglassStillEquipped(player)) {
            zoomManager.disableZoom(playerId);
            return;
        }

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        World world = commandBuffer.getExternalData().getWorld();

        zoomManager.updateZoom(playerId, commandBuffer, world, playerRef);
    }

    public boolean isSpyglassStillEquipped(Player player) {
        var item = player.getInventory().getItemInHand();
        return item != null && item.getItemId().equals(Spyglass.SPYGLASS_ITEM_ID);
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