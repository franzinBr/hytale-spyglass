package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import dev.franzin.spyglass.ZoomManager;

public final class SpyglassHotbarChangedSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
    private final ZoomManager zoomManager;
    private final Query<EntityStore> query = Query.and(Player.getComponentType(), InventoryComponent.Hotbar.getComponentType());
    public SpyglassHotbarChangedSystem(ZoomManager zoomManager) { super(InventoryChangeEvent.class); this.zoomManager = zoomManager; }
    @Override public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commands, @Nonnull InventoryChangeEvent event) {
        if (event.getComponentType() == InventoryComponent.Hotbar.getComponentType())
            SpyglassInventorySystemSupport.disableIfSpyglassIsNotActive(zoomManager, index, chunk, commands, "active hotbar item changed");
    }
    @Nonnull @Override public Query<EntityStore> getQuery() { return query; }
}
