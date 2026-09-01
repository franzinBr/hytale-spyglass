package dev.franzin.spyglass;

import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.ApplyMovementType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoomManagerTest {
    @Test void calculatesAndClampsFov() {
        assertEquals(35.0f, ZoomManager.calculateFov(70, 2, 10, 70));
        assertEquals(70.0f / 3.0f, ZoomManager.calculateFov(70, 3, 10, 70));
        assertEquals(10.0f, ZoomManager.calculateFov(70, 100, 10, 70));
        assertEquals(70.0f, ZoomManager.calculateFov(70, 0.5f, 10, 70));
    }

    @Test void rejectsInvalidInputs() {
        for (float value : new float[] {0, -1, Float.NaN, Float.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class,
                    () -> ZoomManager.calculateFov(70, value, 10, 70));
        assertThrows(IllegalArgumentException.class, () -> ZoomManager.calculateFov(70, 2, 80, 70));
    }

    @Test void calculatesAndClampsControlMultiplier() {
        assertEquals(0.5f, ZoomManager.calculateControlMultiplier(70, 35, 0.1f));
        assertEquals(1.0f / 3.0f, ZoomManager.calculateControlMultiplier(70, 70.0f / 3.0f, 0.1f), 0.00001f);
        assertEquals(1.0f, ZoomManager.calculateControlMultiplier(70, 70, 0.1f));
        assertEquals(0.1f, ZoomManager.calculateControlMultiplier(70, 1, 0.1f));
        assertEquals(1.0f, ZoomManager.calculateControlMultiplier(70, 100, 0.1f));
    }

    @Test void rejectsInvalidControlMultiplierInputs() {
        for (float invalid : new float[] {
                0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> ZoomManager.calculateControlMultiplier(invalid, 35, 0.1f));
            assertThrows(IllegalArgumentException.class,
                    () -> ZoomManager.calculateControlMultiplier(70, invalid, 0.1f));
            assertThrows(IllegalArgumentException.class,
                    () -> ZoomManager.calculateControlMultiplier(70, 35, invalid));
        }
        assertThrows(IllegalArgumentException.class,
                () -> ZoomManager.calculateControlMultiplier(70, 35, 1.01f));
    }

    @Test void buildsCameraSettingsWithScaledControls() {
        ServerCameraSettings settings = ZoomManager.buildCameraSettings(35, 0.5f, true, false);
        assertEquals(35, settings.baseFov);
        assertEquals(0.5f, settings.movementMultiplier.x());
        assertEquals(0.5f, settings.movementMultiplier.y());
        assertEquals(0.5f, settings.movementMultiplier.z());
        assertEquals(0.5f, settings.lookMultiplier.x());
        assertEquals(0.5f, settings.lookMultiplier.y());
        assertEquals(ApplyMovementType.CharacterController, settings.applyMovementType);
        assertEquals(ApplyLookType.LocalPlayerLookOrientation, settings.applyLookType);
        assertTrue(settings.isFirstPerson);
        assertTrue(settings.hideHeldItem);
        assertFalse(settings.displayReticle);
        assertTrue(settings.sendMouseMotion);
        assertTrue(settings.eyeOffset);
    }
}
