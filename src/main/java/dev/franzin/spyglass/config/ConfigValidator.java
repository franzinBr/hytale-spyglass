package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.validation.ValidationResults;

public final class ConfigValidator {
    private static final String PREFIX = "Invalid Spyglass config: ";
    private ConfigValidator() {}

    static void validateForCodec(SpyglassConfig config, ValidationResults results) {
        try { validate(config); }
        catch (IllegalArgumentException exception) { results.fail(exception.getMessage()); }
    }

    public static ZoomSettings validateAndSnapshot(SpyglassConfig config) {
        validate(config);
        return config.zoom().snapshot();
    }

    public static void validate(SpyglassConfig config) {
        if (config == null || config.zoom() == null) fail("Zoom must be an object");
        ZoomSettings zoom = config.zoom();
        positiveFinite("Zoom.ReferenceFov", zoom.referenceFov());
        positiveFinite("Zoom.MinimumFov", zoom.minimumFov());
        positiveFinite("Zoom.MaximumFov", zoom.maximumFov());
        if (zoom.maximumFov() < zoom.minimumFov())
            fail("Zoom.MaximumFov must be greater than or equal to Zoom.MinimumFov");
        float[] levels = zoom.magnificationLevels();
        if (levels == null || levels.length == 0) fail("Zoom.MagnificationLevels must not be empty");
        for (int index = 0; index < levels.length; index++)
            positiveFinite("Zoom.MagnificationLevels[" + index + "]", levels[index]);
        if (zoom.transitionDurationMillis() < 0 || zoom.transitionDurationMillis() > 10_000)
            fail("Zoom.TransitionDurationMillis must be between 0 and 10000");
    }

    private static void positiveFinite(String path, float value) {
        if (!Float.isFinite(value) || value <= 0.0f) fail(path + " must be finite and greater than zero");
    }

    private static void fail(String message) { throw new IllegalArgumentException(PREFIX + message); }
}
