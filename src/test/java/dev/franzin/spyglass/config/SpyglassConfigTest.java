package dev.franzin.spyglass.config;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt64;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpyglassConfigTest {
    @Test void defaultsMatchExistingBehavior() {
        ZoomSettings zoom = ConfigValidator.validateAndSnapshot(new SpyglassConfig());
        assertEquals(70.0f, zoom.referenceFov());
        assertArrayEquals(new float[] {2, 3, 6}, zoom.magnificationLevels());
        assertEquals(10.0f, zoom.minimumFov());
        assertEquals(70.0f, zoom.maximumFov());
        assertEquals(240L, zoom.transitionDurationMillis());
        assertTrue(zoom.hideHeldItem());
        assertTrue(zoom.displayReticle());
    }

    @Test void codecPreservesCompleteConfigurationAndOrder() {
        BsonDocument zoom = new BsonDocument()
                .append("ReferenceFov", new BsonDouble(80))
                .append("MagnificationLevels", new BsonArray(List.of(new BsonDouble(4), new BsonDouble(2))))
                .append("MinimumFov", new BsonDouble(8))
                .append("MaximumFov", new BsonDouble(75))
                .append("TransitionDurationMillis", new BsonInt64(0))
                .append("HideHeldItem", BsonBoolean.FALSE)
                .append("DisplayReticle", BsonBoolean.FALSE);
        ZoomSettings decoded = ConfigValidator.validateAndSnapshot(
                SpyglassConfig.CODEC.decode(new BsonDocument("Zoom", zoom)));
        assertArrayEquals(new float[] {4, 2}, decoded.magnificationLevels());
        assertEquals(0, decoded.transitionDurationMillis());
        assertFalse(decoded.hideHeldItem());
        assertFalse(decoded.displayReticle());
    }

    @Test void omittedFieldKeepsItsDefault() {
        SpyglassConfig decoded = SpyglassConfig.CODEC.decode(new BsonDocument("Zoom",
                new BsonDocument("MinimumFov", new BsonDouble(12))));
        assertEquals(12, decoded.zoom().minimumFov());
        assertEquals(70, decoded.zoom().referenceFov());
    }

    @Test void snapshotAndGettersDefensivelyCopyLevels() {
        SpyglassConfig config = new SpyglassConfig();
        float[] source = {2, 4};
        config.zoom().setMagnificationLevels(source);
        ZoomSettings snapshot = ConfigValidator.validateAndSnapshot(config);
        source[0] = 99;
        float[] returned = snapshot.magnificationLevels();
        returned[0] = 88;
        assertArrayEquals(new float[] {2, 4}, snapshot.magnificationLevels());
    }

    @Test void rejectsInvalidFieldsWithTheirPaths() {
        SpyglassConfig config = new SpyglassConfig();
        config.zoom().setMagnificationLevels(new float[] {2, Float.NaN});
        assertInvalid(config, "Zoom.MagnificationLevels[1]");
        config = new SpyglassConfig(); config.zoom().setReferenceFov(0); assertInvalid(config, "Zoom.ReferenceFov");
        config = new SpyglassConfig(); config.zoom().setMinimumFov(80); assertInvalid(config, "Zoom.MaximumFov");
        config = new SpyglassConfig(); config.zoom().setTransitionDurationMillis(10_001); assertInvalid(config, "Zoom.TransitionDurationMillis");
    }

    private static void assertInvalid(SpyglassConfig config, String path) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ConfigValidator.validate(config));
        assertTrue(error.getMessage().startsWith("Invalid Spyglass config: "));
        assertTrue(error.getMessage().contains(path));
    }
}
