package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InventorySetActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import dev.franzin.spyglass.ZoomManager;

public final class SpyglassActiveSlotChangedSystem extends EntityEventSystem<EntityStore, InventorySetActiveSlotEvent> {
    private final ZoomManager zoomManager;
    private final Query<EntityStore> query = Query.and(Player.getComponentType(), InventoryComponent.Hotbar.getComponentType());
    public SpyglassActiveSlotChangedSystem(ZoomManager zoomManager) { super(InventorySetActiveSlotEvent.class); this.zoomManager = zoomManager; }
    @Override public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commands, @Nonnull InventorySetActiveSlotEvent event) {
        if (event.getInventorySectionId() == InventoryComponent.HOTBAR_SECTION_ID)
            SpyglassInventorySystemSupport.disableIfSpyglassIsNotActive(zoomManager, index, chunk, commands, "active hotbar slot changed");
    }
    @Nonnull @Override public Query<EntityStore> getQuery() { return query; }
}
