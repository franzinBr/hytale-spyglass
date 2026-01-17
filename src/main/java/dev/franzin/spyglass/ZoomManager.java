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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.franzin.spyglass.ui.Clean;
import dev.franzin.spyglass.ui.Spyglass_Overlay;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class ZoomManager {

    private static final ZoomManager INSTANCE = new ZoomManager();

    private final Map<UUID, ZoomState> zoomStates = new ConcurrentHashMap<>();

    private final ZoomConfig config = new ZoomConfig();

    private ZoomManager() {}

    public static ZoomManager getInstance() {
        return INSTANCE;
    }

    public void enableZoom(@Nonnull UUID playerId, @Nonnull Player player, @Nonnull PlayerRef playerRef) {
        ZoomState state = new ZoomState(playerRef, player, config.maxDistance);
        zoomStates.put(playerId, state);
        sendCameraPacket(playerRef, config.maxDistance);
        enableSpyglassOverlayHud(player, playerRef);
    }

    public void disableZoom(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.remove(playerId);
        if (state != null) {
            resetCamera(state.playerRef);
            disableSpyglassOverlayHud(state.player, state.playerRef);
        }
    }

    private void enableSpyglassOverlayHud(Player player, PlayerRef playerRef) {
        player.getHudManager().setCustomHud(playerRef,  new Spyglass_Overlay(playerRef));
    }

    private void disableSpyglassOverlayHud(Player player, PlayerRef playerRef) {
        // that's a crazy workaround to remove the HUD while the API is still broken
        player.getHudManager().setCustomHud(playerRef, new Clean(playerRef));
    }

    public boolean toggleZoom(@Nonnull UUID playerId, @Nonnull Player player, @Nonnull PlayerRef playerRef) {
        if (isZooming(playerId)) {
            disableZoom(playerId);
            return false;
        }
        enableZoom(playerId, player, playerRef);
        return true;
    }

    public boolean isZooming(@Nonnull UUID playerId) {
        return zoomStates.containsKey(playerId);
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

        float targetDistance = calculateSafeDistance(commandBuffer, world, entityRef);

        if (shouldUpdate(state, targetDistance)) {
            state.currentDistance = targetDistance;
            sendCameraPacket(state.playerRef, targetDistance);
        }
    }

    private boolean shouldUpdate(@Nonnull ZoomState state, float newDistance) {
        float diff = Math.abs(newDistance - state.currentDistance);
        boolean movingCloser = newDistance < state.currentDistance;
        return diff > config.updateThreshold || movingCloser;
    }

    private float calculateSafeDistance(
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world,
            @Nonnull Ref<EntityStore> entityRef
    ) {
        Transform transform = TargetUtil.getLook(entityRef, commandBuffer);
        Vector3d position = transform.getPosition();
        Vector3d direction = transform.getDirection();

        Vector3i hitBlock = TargetUtil.getTargetBlock(
                world,
                (blockId, fluidId) -> blockId != 0,
                position.x, position.y, position.z,
                direction.x, direction.y, direction.z,
                config.maxDistance
        );


        if (hitBlock == null) {
            return config.maxDistance;
        }

        double dx = hitBlock.x + 0.5 - position.x;
        double dy = hitBlock.y + 0.5 - position.y;
        double dz = hitBlock.z + 0.5 - position.z;
        float distanceToBlock = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float safeDistance = distanceToBlock - config.collisionMargin;
        return clamp(safeDistance, config.minDistance, config.maxDistance);
    }

    private void sendCameraPacket(@Nonnull PlayerRef playerRef, float distance) {
        ServerCameraSettings settings = buildSettings(distance);
        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, settings)
        );
    }

    private void resetCamera(@Nonnull PlayerRef playerRef) {
        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, null)
        );
    }

    private ServerCameraSettings buildSettings(float distance) {

        ServerCameraSettings s = new ServerCameraSettings();
        s.isFirstPerson = false;
        s.distance = -distance;
        s.eyeOffset = true;
        s.positionLerpSpeed = 0.2F;
        s.rotationLerpSpeed = 0.01F;
        s.movementMultiplier = new Vector3f(0.33F, 0.33F, 0.33F);
        s.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
        s.sendMouseMotion = true;
        s.displayReticle = true;
        return s;

    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class ZoomState {
        final PlayerRef playerRef;
        final Player player;
        float currentDistance;

        ZoomState(PlayerRef playerRef,  Player player, float initialDistance) {
            this.playerRef = playerRef;
            this.player = player;
            this.currentDistance = initialDistance;
        }
    }

    public static class ZoomConfig {
        public float maxDistance = 20.0f;
        public float minDistance = 1.0f;
        public float collisionMargin = 1.0f;
        public float updateThreshold = 0.3f;
    }
}