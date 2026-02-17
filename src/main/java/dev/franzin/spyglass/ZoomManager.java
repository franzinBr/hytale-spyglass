/*
 * Copyright (c) 2026 Alan Franzin
 * SPDX-License-Identifier: MIT
 */

package dev.franzin.spyglass;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.franzin.spyglass.ui.Spyglass_Overlay;
import dev.franzin.spyglass.ui.hudmanager.UIManager;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class ZoomManager {

    private static final ZoomManager INSTANCE = new ZoomManager();

    private final Map<UUID, ZoomState> zoomStates = new ConcurrentHashMap<>();

    private ZoomManager() {}

    public static ZoomManager getInstance() {
        return INSTANCE;
    }

    public void enableZoom(
            @Nonnull UUID playerId,
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            @Nonnull ZoomSettings settings
    ) {
        ZoomState state = new ZoomState(playerRef, player, settings);
        zoomStates.put(playerId, state);
        sendCameraPacket(playerRef, settings.maxDistance());
        enableSpyglassOverlayHud(player, playerRef, settings.overlayTexturePath());
        playSpyglassOpenSound(playerRef);
    }

    public void disableZoom(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.remove(playerId);
        if (state != null) {
            resetCamera(state.playerRef);
            disableSpyglassOverlayHud(state.player, state.playerRef);
            playSpyglassCloseSound(state.playerRef);
        }
    }

    private void enableSpyglassOverlayHud(Player player, PlayerRef playerRef, String overlayTexturePath) {
        if(UIManager.getInstance().hasHudConflict(player)) {
            sendDisabledHudMessage(player);
            return;
        }

        UIManager.getInstance().setCustomHud(
                player,
                playerRef,
                "spyglass-overlay",
                new Spyglass_Overlay(playerRef, overlayTexturePath)
        );

    }

    private void disableSpyglassOverlayHud(Player player, PlayerRef playerRef) {
        if(UIManager.getInstance().hasHudConflict(player)) {
            sendDisabledHudMessage(player);
            return;
        }

        UIManager.getInstance().hideCustomHud(player, playerRef, "spyglass-overlay");
    }

    private void sendDisabledHudMessage(Player player) {
        player.sendMessage(
                Message.raw("[Spyglass] HUD feature disabled. You have other HUD mods but MultipleHUD is missing.")
                        .color("#FF6600")
        );
        player.sendMessage(
                Message.raw("[Spyglass ] Install MultipleHUD to allow the spyglass HUD to work with other mods without crashes: curseforge.com/hytale/mods/multiplehud")
                        .color("#AAAAAA")
        );
    }

    private void playSpyglassOpenSound(PlayerRef playerRef) {
        int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Spyglass_Open");
        SoundUtil.playSoundEvent2dToPlayer(playerRef, soundEventIndex, SoundCategory.SFX);
    }

    private void playSpyglassCloseSound(PlayerRef playerRef) {
        int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Spyglass_Close");
        SoundUtil.playSoundEvent2dToPlayer(playerRef, soundEventIndex, SoundCategory.SFX);
    }

    public boolean toggleZoom(
            @Nonnull UUID playerId,
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            @Nonnull ZoomSettings settings
    ) {
        if (isZooming(playerId)) {
            disableZoom(playerId);
            return false;
        }
        enableZoom(playerId, player, playerRef, settings);
        return true;
    }

    public boolean isZooming(@Nonnull UUID playerId) {
        return zoomStates.containsKey(playerId);
    }

    public boolean isZoomConfigMatching(@Nonnull UUID playerId, @Nonnull ZoomSettings settings) {
        ZoomState state = zoomStates.get(playerId);
        return state != null && state.settings.equals(settings);
    }

    public void zoomIn(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.get(playerId);

        if (state == null) {
            return;
        }

        float newZoomMultiplier = state.zoomMultiplier + state.settings.zoomMultiplierStep();
        if (newZoomMultiplier > state.settings.maxZoomMultiplier()) {
            newZoomMultiplier = state.settings.maxZoomMultiplier();
        }

        playSpyglassOpenSound(state.playerRef);
        state.zoomMultiplier = newZoomMultiplier;
    }

    public void zoomOut(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.get(playerId);

        if (state == null) {
            return;
        }

        float newZoomMultiplier = state.zoomMultiplier - state.settings.zoomMultiplierStep();
        if (newZoomMultiplier < state.settings.defaultZoomMultiplier()) {
            newZoomMultiplier = state.settings.defaultZoomMultiplier();
        }

        playSpyglassCloseSound(state.playerRef);
        state.zoomMultiplier = newZoomMultiplier;
    }

    public void zoomCycle(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.get(playerId);

        if (state == null) {
            return;
        }

        float newZoomMultiplier = state.zoomMultiplier + state.settings.zoomMultiplierStep();
        if (newZoomMultiplier > state.settings.maxZoomMultiplier()) {
            newZoomMultiplier = state.settings.defaultZoomMultiplier();
        }

        playSpyglassOpenSound(state.playerRef);
        state.zoomMultiplier = newZoomMultiplier;
    }

    public void updateZoom(
            @Nonnull UUID playerId,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world,
            @Nonnull Ref<EntityStore> entityRef
    ) {
        ZoomState state = zoomStates.get(playerId);
        if (state == null) {
            return;
        }

        float targetDistance = calculateSafeDistance(commandBuffer, world, entityRef, state);

        if (shouldUpdate(state, targetDistance)) {
            state.currentDistance = targetDistance;
            sendCameraPacket(state.playerRef, targetDistance);
        }
    }

    private boolean shouldUpdate(@Nonnull ZoomState state, float newDistance) {
        return state.currentDistance != newDistance;
    }

    private float calculateSafeDistance(
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull ZoomState state
    ) {
        Transform transform = TargetUtil.getLook(entityRef, commandBuffer);
        Vector3d position = transform.getPosition();
        Vector3d direction = transform.getDirection();
        float maxDistance = state.settings.maxDistance() * state.zoomMultiplier;

        Vector3i hitBlock = TargetUtil.getTargetBlock(
                world,
                (blockId, fluidId) -> blockId != 0,
                position.x, position.y, position.z,
                direction.x, direction.y, direction.z,
                maxDistance
        );

        if (hitBlock == null) {
            return maxDistance;
        }

        double dx = hitBlock.x + 0.5 - position.x;
        double dy = hitBlock.y + 0.5 - position.y;
        double dz = hitBlock.z + 0.5 - position.z;
        float distanceToBlock = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float collisionMargin = computeCollisionMargin(maxDistance);
        float safeDistance = distanceToBlock - collisionMargin;

        return Math.clamp(safeDistance, state.settings.minDistance(), maxDistance);
    }

    private float computeCollisionMargin(float targetDistance) {
        float scaledMargin = ZoomConfig.BASE_COLLISION_MARGIN + (targetDistance * ZoomConfig.COLLISION_SCALE_FACTOR);
        return Math.min(scaledMargin, ZoomConfig.MAX_COLLISION_MARGIN);
    }

    private void sendCameraPacket(@Nonnull PlayerRef playerRef, float distance) {
        ServerCameraSettings settings = buildSettings(distance);
        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, settings)
        );
    }

    private void resetCamera(@Nonnull PlayerRef playerRef) {
        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, null)
        );
    }

    private ServerCameraSettings buildSettings(float distance) {

        ServerCameraSettings s = new ServerCameraSettings();
        s.isFirstPerson = false;
        s.distance = -distance;
        s.eyeOffset = true;
        s.positionLerpSpeed = 0.2F;
        s.rotationLerpSpeed = 0.1F;
        s.movementMultiplier = new Vector3f(0.33F, 0.33F, 0.33F);
        s.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
        s.sendMouseMotion = true;
        s.displayReticle = true;
        return s;

    }

    private static class ZoomState {
        final PlayerRef playerRef;
        final Player player;
        final ZoomSettings settings;
        float currentDistance;
        float zoomMultiplier;

        ZoomState(PlayerRef playerRef, Player player, ZoomSettings settings) {
            this.playerRef = playerRef;
            this.player = player;
            this.settings = settings;
            this.currentDistance = settings.maxDistance();
            this.zoomMultiplier = settings.defaultZoomMultiplier();
        }
    }

    public record ZoomSettings(
            float maxDistance,
            float minDistance,
            float defaultZoomMultiplier,
            float maxZoomMultiplier,
            float zoomMultiplierStep,
            String overlayTexturePath
    ) {
        public static ZoomSettings of(
                float maxDistance,
                float minDistance,
                float defaultZoomMultiplier,
                float maxZoomMultiplier,
                float zoomMultiplierStep,
                String overlayTexturePath
        ) {
            float safeMaxDistance = Math.max(0.5f, maxDistance);
            float safeMinDistance = Math.max(0.1f, Math.min(minDistance, safeMaxDistance));

            float safeDefaultMultiplier = Math.max(0.1f, defaultZoomMultiplier);
            float safeMaxMultiplier = Math.max(safeDefaultMultiplier, maxZoomMultiplier);
            float safeMultiplierStep = Math.max(0.01f, zoomMultiplierStep);

            String safeOverlay = (overlayTexturePath == null || overlayTexturePath.isBlank())
                    ? null
                    : overlayTexturePath;

            return new ZoomSettings(
                    safeMaxDistance,
                    safeMinDistance,
                    safeDefaultMultiplier,
                    safeMaxMultiplier,
                    safeMultiplierStep,
                    safeOverlay
            );
        }
    }

    public static class ZoomConfig {
        public static final float MAX_DISTANCE = 20.0f;
        public static final float MIN_DISTANCE = 1.0f;

        public static final float DEFAULT_ZOOM_MULTIPLIER = 1.0f;
        public static final float MAX_ZOOM_MULTIPLIER = 2.5f;
        public static final float ZOOM_MULTIPLIER_STEP = 0.5f;

        public static final float BASE_COLLISION_MARGIN = 3.0f;
        public static final float COLLISION_SCALE_FACTOR = 0.05f;
        public static final float MAX_COLLISION_MARGIN = 4.0f;
    }
}
