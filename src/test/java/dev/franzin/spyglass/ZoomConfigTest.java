package dev.franzin.spyglass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoomConfigTest {
    @Test void calculatesConfiguredLevels() {
        assertEquals(35.0f, ZoomManager.ZoomConfig.calculateFov(70, 2, 10, 70));
        assertEquals(17.5f, ZoomManager.ZoomConfig.calculateFov(70, 4, 10, 70));
        assertEquals(70.0f / 6.0f, ZoomManager.ZoomConfig.fovForLevel(2));
    }

    @Test void clampsToSafeRange() {
        assertEquals(10.0f, ZoomManager.ZoomConfig.calculateFov(70, 100, 10, 70));
        assertEquals(70.0f, ZoomManager.ZoomConfig.calculateFov(70, 0.5f, 10, 70));
    }

    @Test void rejectsInvalidMagnification() {
        for (float value : new float[] {0, -1, Float.NaN, Float.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class,
                    () -> ZoomManager.ZoomConfig.calculateFov(70, value, 10, 70));
    }

    @Test void rejectsEmptyAndInvalidLevelLists() {
        assertThrows(IllegalArgumentException.class, ZoomManager.ZoomConfig::validateLevels);
        assertThrows(IllegalArgumentException.class, () -> ZoomManager.ZoomConfig.validateLevels(2, 0));
        assertArrayEquals(new float[] {2, 3, 6}, ZoomManager.ZoomConfig.validateLevels(2, 3, 6));
    }
}
