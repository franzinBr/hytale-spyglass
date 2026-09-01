package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.franzin.spyglass.Spyglass;
import dev.franzin.spyglass.ZoomManager;

final class SpyglassInventorySystemSupport {
    private SpyglassInventorySystemSupport() {}
    static void disableIfSpyglassIsNotActive(ZoomManager zoomManager, int index, ArchetypeChunk<EntityStore> chunk,
                                              CommandBuffer<EntityStore> commandBuffer, String reason) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = player == null ? null : chunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null || !zoomManager.isZooming(playerRef.getUuid())) return;
        ItemStack item = InventoryComponent.getItemInHand(commandBuffer, chunk.getReferenceTo(index));
        if (ItemStack.isEmpty(item) || !Spyglass.SPYGLASS_ITEM_ID.equals(item.getItemId()))
            zoomManager.disableZoom(playerRef.getUuid(), reason);
    }
}
