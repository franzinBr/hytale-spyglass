package dev.franzin.spyglass.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import dev.franzin.spyglass.config.SkeletonPirateDropSettings;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;

import static dev.franzin.spyglass.Spyglass.SPYGLASS_ITEM_ID;

/** Adds the configured spyglass drop without replacing the NPC's native drop list. */
public final class SkeletonPirateSpyglassDropSystem extends DeathSystems.OnDeathSystem {
    static final String CAPTAIN_ROLE = "Skeleton_Pirate_Captain";
    static final String GUNNER_ROLE = "Skeleton_Pirate_Gunner";
    static final String STRIKER_ROLE = "Skeleton_Pirate_Striker";

    private final SkeletonPirateDropSettings.Snapshot settings;
    private final DoubleSupplier random;
    private final BiConsumer<String, Throwable> errorLogger;

    public SkeletonPirateSpyglassDropSystem(SkeletonPirateDropSettings.Snapshot settings,
                                             DoubleSupplier random,
                                             BiConsumer<String, Throwable> errorLogger) {
        this.settings = settings;
        this.random = random;
        this.errorLogger = errorLogger;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                NPCEntity.getComponentType(),
                TransformComponent.getComponentType(),
                HeadRotation.getComponentType(),
                Query.not(Player.getComponentType())
        );
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent death,
                                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null) return;
        String role = npc.getRoleName();
        float chance = chanceForRole(settings, role);
        if (!shouldDrop(chance, random)) return;

        try {
            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
            if (transform == null || headRotation == null)
                throw new IllegalStateException("required loot-position component is missing");
            ItemStack item = new ItemStack(SPYGLASS_ITEM_ID, 1);
            if (!item.isValid()) throw new IllegalStateException("item is not registered");
            Vector3d lootPosition = new Vector3d(transform.getPosition()).add(0.0, 1.0, 0.0);
            Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(
                    store, List.of(item), lootPosition, headRotation.getRotation());
            commandBuffer.addEntities(drops, AddReason.SPAWN);
        } catch (RuntimeException exception) {
            errorLogger.accept("Failed to materialize drop for role " + role + " and ItemId "
                    + SPYGLASS_ITEM_ID, exception);
        }
    }

    static float chanceForRole(SkeletonPirateDropSettings.Snapshot settings, String role) {
        if (CAPTAIN_ROLE.equals(role)) return settings.captainChancePercent();
        if (GUNNER_ROLE.equals(role)) return settings.gunnerChancePercent();
        if (STRIKER_ROLE.equals(role)) return settings.strikerChancePercent();
        return 0.0f;
    }

    static boolean shouldDrop(float chancePercent, DoubleSupplier random) {
        if (chancePercent <= 0.0f) return false;
        if (chancePercent >= 100.0f) return true;
        return random.getAsDouble() * 100.0 < chancePercent;
    }
}
