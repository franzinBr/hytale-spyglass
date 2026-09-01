package dev.franzin.spyglass;

import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.ApplyMovementType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import dev.franzin.spyglass.ui.Spyglass_Overlay;
import dev.franzin.spyglass.ui.hudmanager.UIManager;
import dev.franzin.spyglass.config.ZoomSettings;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.joml.Vector2f;
import org.joml.Vector3f;

/** Owns the persistent, per-player native-FOV zoom state. */
public final class ZoomManager {
    private static final int TRANSITION_STEPS = 7;
    private static final String OVERLAY_ID = "spyglass-overlay";
    private final Map<UUID, ZoomState> zoomStates = new ConcurrentHashMap<>();
    private final Spyglass plugin;
    private final ZoomSettings settings;
    private final float[] magnificationLevels;

    public ZoomManager(@Nonnull Spyglass plugin, @Nonnull ZoomSettings settings) {
        this.plugin = plugin;
        this.settings = settings.snapshot();
        this.magnificationLevels = this.settings.magnificationLevels();
    }

    public boolean toggleZoom(@Nonnull UUID playerId, @Nonnull Player player, @Nonnull PlayerRef playerRef) {
        if (isZooming(playerId)) {
            disableZoom(playerId, "toggle");
            return false;
        }
        ZoomState state = new ZoomState(player, playerRef, 0, settings.referenceFov());
        zoomStates.put(playerId, state);
        try {
            applyLevel(playerId, state);
            UIManager.getInstance().setCustomHud(player, playerRef, OVERLAY_ID, new Spyglass_Overlay(playerRef));
            playSound(playerRef, "SFX_Spyglass_Open");
            debug("Zoom enabled for " + playerId);
            return true;
        } catch (RuntimeException exception) {
            zoomStates.remove(playerId, state);
            restoreAfterFailure(playerId, state, exception);
            throw exception;
        }
    }

    public boolean stepZoom(@Nonnull UUID playerId) {
        ZoomState state = zoomStates.get(playerId);
        if (state == null) return false;
        state.levelIndex = (state.levelIndex + 1) % magnificationLevels.length;
        applyLevel(playerId, state);
        playSound(state.playerRef, "SFX_Spyglass_Open");
        return true;
    }

    public boolean isZooming(@Nonnull UUID playerId) { return zoomStates.containsKey(playerId); }

    public void disableZoom(@Nonnull UUID playerId) { disableZoom(playerId, "requested"); }

    public void disableZoom(@Nonnull UUID playerId, @Nonnull String reason) {
        ZoomState state = zoomStates.remove(playerId);
        if (state == null) return;
        try {
            resetCamera(state.playerRef);
        } catch (RuntimeException exception) {
            log(Level.WARNING, "Failed to restore camera for " + playerId, exception);
        }
        try {
            UIManager.getInstance().hideCustomHud(state.player, state.playerRef, OVERLAY_ID);
            playSound(state.playerRef, "SFX_Spyglass_Close");
        } catch (RuntimeException exception) {
            log(Level.WARNING, "Failed to clean up zoom UI for " + playerId, exception);
        }
        debug("Zoom disabled for " + playerId + " (" + reason + ")");
    }

    public void disableAll() {
        for (UUID playerId : new ArrayList<>(zoomStates.keySet())) disableZoom(playerId, "plugin shutdown");
        zoomStates.clear();
    }

    private void applyLevel(UUID playerId, ZoomState state) {
        float targetFov = calculateFov(settings.referenceFov(), magnificationLevels[state.levelIndex],
                settings.minimumFov(), settings.maximumFov());
        float sourceFov = state.currentFov;
        int transitionId = ++state.transitionId;
        state.currentFov = targetFov;

        long duration = settings.transitionDurationMillis();
        if (duration == 0) {
            sendCameraPacket(state.playerRef, targetFov, controlMultiplier(targetFov));
            return;
        }

        int steps = (int) Math.min(TRANSITION_STEPS, duration);

        for (int step = 1; step <= steps; step++) {
            final int scheduledStep = step;
            Callable<Void> update = () -> {
                if (zoomStates.get(playerId) == state && state.transitionId == transitionId) {
                    float progress = (float) scheduledStep / steps;
                    float easedProgress = progress * progress * (3.0f - 2.0f * progress);
                    float interpolatedFov = sourceFov + (targetFov - sourceFov) * easedProgress;
                    sendCameraPacket(state.playerRef, interpolatedFov, controlMultiplier(interpolatedFov));
                }
                return null;
            };
            ScheduledFuture<Void> task = HytaleServer.SCHEDULED_EXECUTOR.schedule(
                    update, Math.round((double) scheduledStep * duration / steps), TimeUnit.MILLISECONDS);
            plugin.getTaskRegistry().registerTask(task);
        }
        debug("Applied zoom level " + state.levelIndex + " (FOV " + targetFov + ") for " + playerId);
    }

    private float controlMultiplier(float fov) {
        return settings.scaleControlsWithZoom()
                ? calculateControlMultiplier(settings.referenceFov(), fov, settings.minimumControlMultiplier())
                : 1.0f;
    }

    private void sendCameraPacket(PlayerRef playerRef, float fov, float controlMultiplier) {
        ServerCameraSettings settings = buildCameraSettings(
                fov, controlMultiplier, this.settings.hideHeldItem(), this.settings.displayReticle());
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, settings));
    }

    static ServerCameraSettings buildCameraSettings(
            float fov, float controlMultiplier, boolean hideHeldItem, boolean displayReticle) {
        ServerCameraSettings settings = new ServerCameraSettings();
        settings.isFirstPerson = true;
        settings.hideHeldItem = hideHeldItem;
        settings.displayReticle = displayReticle;
        settings.sendMouseMotion = true;
        settings.eyeOffset = true;
        settings.baseFov = fov;
        settings.applyMovementType = ApplyMovementType.CharacterController;
        settings.movementMultiplier = new Vector3f(controlMultiplier, controlMultiplier, controlMultiplier);
        settings.applyLookType = ApplyLookType.LocalPlayerLookOrientation;
        settings.lookMultiplier = new Vector2f(controlMultiplier, controlMultiplier);
        return settings;
    }

    private void resetCamera(PlayerRef playerRef) {
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, null));
    }

    private void restoreAfterFailure(UUID playerId, ZoomState state, RuntimeException original) {
        try { resetCamera(state.playerRef); }
        catch (RuntimeException failure) {
            original.addSuppressed(failure);
            log(Level.WARNING, "Failed to restore camera after activation failure for " + playerId, failure);
        }
        try { UIManager.getInstance().hideCustomHud(state.player, state.playerRef, OVERLAY_ID); }
        catch (RuntimeException failure) { original.addSuppressed(failure); }
    }

    private void playSound(PlayerRef playerRef, String id) {
        SoundUtil.playSoundEvent2dToPlayer(playerRef, SoundEvent.getAssetMap().getIndex(id), SoundCategory.SFX);
    }

    public static float calculateFov(float reference, float magnification, float minimum, float maximum) {
        if (!Float.isFinite(magnification) || magnification <= 0.0f)
            throw new IllegalArgumentException("Magnification must be finite and greater than zero");
        if (!Float.isFinite(reference) || !Float.isFinite(minimum) || !Float.isFinite(maximum)
                || reference <= 0.0f || minimum <= 0.0f || maximum <= 0.0f || minimum > maximum)
            throw new IllegalArgumentException("Invalid FOV configuration");
        return Math.clamp(reference / magnification, minimum, maximum);
    }

    public static float calculateControlMultiplier(float referenceFov, float targetFov, float minimumMultiplier) {
        if (!Float.isFinite(referenceFov) || !Float.isFinite(targetFov)
                || referenceFov <= 0.0f || targetFov <= 0.0f)
            throw new IllegalArgumentException("FOV values must be finite and greater than zero");
        if (!Float.isFinite(minimumMultiplier) || minimumMultiplier <= 0.0f || minimumMultiplier > 1.0f)
            throw new IllegalArgumentException("Minimum multiplier must be finite and in (0, 1]");
        return Math.clamp(targetFov / referenceFov, minimumMultiplier, 1.0f);
    }

    private void debug(String message) { log(Level.FINE, message, null); }

    private void log(Level level, String message, Throwable exception) {
        if (exception == null) plugin.getLogger().at(level).log(message);
        else plugin.getLogger().at(level).withCause(exception).log(message);
    }

    private static final class ZoomState {
        private final Player player;
        private final PlayerRef playerRef;
        private int levelIndex;
        private volatile int transitionId;
        private volatile float currentFov;
        private ZoomState(Player player, PlayerRef playerRef, int levelIndex, float currentFov) {
            this.player = player;
            this.playerRef = playerRef;
            this.levelIndex = levelIndex;
            this.currentFov = currentFov;
        }
    }

}
