package dev.franzin.spyglass;

import com.hypixel.hytale.protocol.ClientCameraView;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/** Owns the persistent, per-player native-FOV zoom state. */
public final class ZoomManager {
    private static final ZoomManager INSTANCE = new ZoomManager();
    private static final String OVERLAY_ID = "spyglass-overlay";
    private final Map<UUID, ZoomState> zoomStates = new ConcurrentHashMap<>();

    private ZoomManager() {}

    public static ZoomManager getInstance() { return INSTANCE; }

    public boolean toggleZoom(@Nonnull UUID playerId, @Nonnull Player player, @Nonnull PlayerRef playerRef) {
        if (isZooming(playerId)) {
            disableZoom(playerId, "toggle");
            return false;
        }
        ZoomState state = new ZoomState(player, playerRef, 0, ZoomConfig.REFERENCE_FOV);
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
        state.levelIndex = (state.levelIndex + 1) % ZoomConfig.levelCount();
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
        float targetFov = ZoomConfig.fovForLevel(state.levelIndex);
        float sourceFov = state.currentFov;
        int transitionId = ++state.transitionId;
        state.currentFov = targetFov;

        for (int step = 1; step <= ZoomConfig.TRANSITION_STEPS; step++) {
            final int scheduledStep = step;
            Callable<Void> update = () -> {
                if (zoomStates.get(playerId) == state && state.transitionId == transitionId) {
                    float progress = (float) scheduledStep / ZoomConfig.TRANSITION_STEPS;
                    float easedProgress = progress * progress * (3.0f - 2.0f * progress);
                    sendCameraPacket(state.playerRef,
                            sourceFov + (targetFov - sourceFov) * easedProgress);
                }
                return null;
            };
            ScheduledFuture<Void> task = HytaleServer.SCHEDULED_EXECUTOR.schedule(
                    update, (long) (step - 1) * ZoomConfig.TRANSITION_STEP_MILLIS, TimeUnit.MILLISECONDS);
            Spyglass.getInstance().getTaskRegistry().registerTask(task);
        }
        debug("Applied zoom level " + state.levelIndex + " (FOV " + targetFov + ") for " + playerId);
    }

    private void sendCameraPacket(PlayerRef playerRef, float fov) {
        ServerCameraSettings settings = new ServerCameraSettings();
        settings.isFirstPerson = true;
        settings.hideHeldItem = ZoomConfig.HIDE_HELD_ITEM;
        settings.displayReticle = true;
        settings.sendMouseMotion = true;
        settings.eyeOffset = true;
        settings.baseFov = fov;
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, settings));
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

    private static void debug(String message) { log(Level.FINE, message, null); }

    private static void log(Level level, String message, Throwable exception) {
        if (exception == null) Spyglass.getInstance().getLogger().at(level).log(message);
        else Spyglass.getInstance().getLogger().at(level).withCause(exception).log(message);
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

    public static final class ZoomConfig {
        static final float REFERENCE_FOV = 70.0f;
        static final float MINIMUM_FOV = 10.0f;
        static final float MAXIMUM_FOV = 70.0f;
        static final boolean HIDE_HELD_ITEM = true;
        static final int TRANSITION_STEPS = 7;
        static final long TRANSITION_STEP_MILLIS = 40L;
        private static final float[] MAGNIFICATION_LEVELS = validateLevels(2.0f, 3.0f, 6.0f);
        private ZoomConfig() {}
        static int levelCount() { return MAGNIFICATION_LEVELS.length; }
        public static float calculateFov(float reference, float magnification, float minimum, float maximum) {
            if (!Float.isFinite(magnification) || magnification <= 0.0f)
                throw new IllegalArgumentException("Magnification must be finite and greater than zero");
            if (!Float.isFinite(reference) || !Float.isFinite(minimum) || !Float.isFinite(maximum) || minimum > maximum)
                throw new IllegalArgumentException("Invalid FOV configuration");
            return Math.clamp(reference / magnification, minimum, maximum);
        }
        static float fovForLevel(int index) {
            return calculateFov(REFERENCE_FOV, MAGNIFICATION_LEVELS[index], MINIMUM_FOV, MAXIMUM_FOV);
        }
        static float[] validateLevels(float... levels) {
            if (levels == null || levels.length == 0)
                throw new IllegalArgumentException("At least one magnification level is required");
            float[] copy = levels.clone();
            for (float level : copy) if (!Float.isFinite(level) || level <= 0.0f)
                throw new IllegalArgumentException("Magnification levels must be finite and greater than zero");
            return copy;
        }
    }
}
