package dev.franzin.spyglass.system;

import dev.franzin.spyglass.config.SkeletonPirateDropSettings;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

class SkeletonPirateSpyglassDropSystemTest {
    private static final SkeletonPirateDropSettings.Snapshot SETTINGS =
            new SkeletonPirateDropSettings.Snapshot(8, 4, 2);

    @Test void mapsOnlyTheThreeExactRoles() {
        assertEquals(8, SkeletonPirateSpyglassDropSystem.chanceForRole(SETTINGS, "Skeleton_Pirate_Captain"));
        assertEquals(4, SkeletonPirateSpyglassDropSystem.chanceForRole(SETTINGS, "Skeleton_Pirate_Gunner"));
        assertEquals(2, SkeletonPirateSpyglassDropSystem.chanceForRole(SETTINGS, "Skeleton_Pirate_Striker"));
        assertEquals(0, SkeletonPirateSpyglassDropSystem.chanceForRole(SETTINGS, "skeleton_pirate_captain"));
        assertEquals(0, SkeletonPirateSpyglassDropSystem.chanceForRole(SETTINGS, "Skeleton_Pirate"));
    }

    @Test void zeroAndHundredDoNotConsumeRandomness() {
        AtomicInteger calls = new AtomicInteger();
        DoubleSupplier random = () -> { calls.incrementAndGet(); return 0.5; };
        assertFalse(SkeletonPirateSpyglassDropSystem.shouldDrop(0, random));
        assertTrue(SkeletonPirateSpyglassDropSystem.shouldDrop(100, random));
        assertEquals(0, calls.get());
    }

    @Test void intermediateChanceUsesStrictBoundaryAndOneRoll() {
        AtomicInteger calls = new AtomicInteger();
        assertFalse(SkeletonPirateSpyglassDropSystem.shouldDrop(8,
                () -> { calls.incrementAndGet(); return 0.08; }));
        assertEquals(1, calls.get());
        assertTrue(SkeletonPirateSpyglassDropSystem.shouldDrop(8, () -> 0.079999));
    }
}
