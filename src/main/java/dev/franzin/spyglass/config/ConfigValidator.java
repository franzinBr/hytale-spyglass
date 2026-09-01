package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.validation.ValidationResults;

public final class ConfigValidator {
    private static final String PREFIX = "Invalid Spyglass config: ";
    private ConfigValidator() {}

    static void validateForCodec(SpyglassConfig config, ValidationResults results) {
        try { validate(config); }
        catch (IllegalArgumentException exception) { results.fail(exception.getMessage()); }
    }

    public static Snapshot validateAndSnapshot(SpyglassConfig config) {
        validate(config);
        return new Snapshot(config.zoom().snapshot(), config.drops().skeletonPirates().snapshot());
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
        if (!Float.isFinite(zoom.minimumControlMultiplier())
                || zoom.minimumControlMultiplier() <= 0.0f || zoom.minimumControlMultiplier() > 1.0f)
            fail("Zoom.MinimumControlMultiplier must be finite and between zero (exclusive) and one (inclusive)");
        if (config.drops() == null) fail("Drops must be an object");
        if (config.drops().skeletonPirates() == null) fail("Drops.SkeletonPirates must be an object");
        SkeletonPirateDropSettings pirates = config.drops().skeletonPirates();
        percent("Drops.SkeletonPirates.CaptainChancePercent", pirates.captainChancePercent());
        percent("Drops.SkeletonPirates.GunnerChancePercent", pirates.gunnerChancePercent());
        percent("Drops.SkeletonPirates.StrikerChancePercent", pirates.strikerChancePercent());
    }

    private static void positiveFinite(String path, float value) {
        if (!Float.isFinite(value) || value <= 0.0f) fail(path + " must be finite and greater than zero");
    }

    private static void percent(String path, float value) {
        if (!Float.isFinite(value) || value < 0.0f || value > 100.0f)
            fail(path + " must be between 0 and 100");
    }

    public record Snapshot(ZoomSettings zoom, SkeletonPirateDropSettings.Snapshot skeletonPirates) {}

    private static void fail(String message) { throw new IllegalArgumentException(PREFIX + message); }
}
