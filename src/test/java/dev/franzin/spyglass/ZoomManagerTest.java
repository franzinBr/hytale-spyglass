package dev.franzin.spyglass;

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
}
