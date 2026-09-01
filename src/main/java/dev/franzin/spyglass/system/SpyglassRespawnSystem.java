package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.RespawnEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.ZoomManager;
import javax.annotation.Nonnull;

public final class SpyglassRespawnSystem extends EntityEventSystem<EntityStore, RespawnEvent> {
    private final ZoomManager zoomManager;
    public SpyglassRespawnSystem(ZoomManager zoomManager) { super(RespawnEvent.class); this.zoomManager = zoomManager; }
    @Override public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commands, @Nonnull RespawnEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : chunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef != null) zoomManager.disableZoom(playerRef.getUuid(), "player respawned");
    }
    @Nonnull @Override public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
