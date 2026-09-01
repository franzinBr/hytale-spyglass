package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/** Zoom configuration decoded at plugin preload time. */
public final class ZoomSettings {
    public static final BuilderCodec<ZoomSettings> CODEC = BuilderCodec.builder(ZoomSettings.class, ZoomSettings::new)
            .addField(new KeyedCodec<>("ReferenceFov", Codec.FLOAT), ZoomSettings::setReferenceFov, ZoomSettings::referenceFov)
            .addField(new KeyedCodec<>("MagnificationLevels", Codec.FLOAT_ARRAY), ZoomSettings::setMagnificationLevels, ZoomSettings::magnificationLevels)
            .addField(new KeyedCodec<>("MinimumFov", Codec.FLOAT), ZoomSettings::setMinimumFov, ZoomSettings::minimumFov)
            .addField(new KeyedCodec<>("MaximumFov", Codec.FLOAT), ZoomSettings::setMaximumFov, ZoomSettings::maximumFov)
            .addField(new KeyedCodec<>("TransitionDurationMillis", Codec.LONG), ZoomSettings::setTransitionDurationMillis, ZoomSettings::transitionDurationMillis)
            .addField(new KeyedCodec<>("HideHeldItem", Codec.BOOLEAN), ZoomSettings::setHideHeldItem, ZoomSettings::hideHeldItem)
            .addField(new KeyedCodec<>("DisplayReticle", Codec.BOOLEAN), ZoomSettings::setDisplayReticle, ZoomSettings::displayReticle)
            .addField(new KeyedCodec<>("ScaleControlsWithZoom", Codec.BOOLEAN), ZoomSettings::setScaleControlsWithZoom, ZoomSettings::scaleControlsWithZoom)
            .addField(new KeyedCodec<>("MinimumControlMultiplier", Codec.FLOAT), ZoomSettings::setMinimumControlMultiplier, ZoomSettings::minimumControlMultiplier)
            .build();

    private float referenceFov = 70.0f;
    private float[] magnificationLevels = {2.0f, 3.0f, 6.0f};
    private float minimumFov = 10.0f;
    private float maximumFov = 70.0f;
    private long transitionDurationMillis = 240L;
    private boolean hideHeldItem = true;
    private boolean displayReticle = true;
    private boolean scaleControlsWithZoom = true;
    private float minimumControlMultiplier = 0.1f;

    public ZoomSettings() {}

    private ZoomSettings(ZoomSettings source) {
        referenceFov = source.referenceFov;
        magnificationLevels = source.magnificationLevels.clone();
        minimumFov = source.minimumFov;
        maximumFov = source.maximumFov;
        transitionDurationMillis = source.transitionDurationMillis;
        hideHeldItem = source.hideHeldItem;
        displayReticle = source.displayReticle;
        scaleControlsWithZoom = source.scaleControlsWithZoom;
        minimumControlMultiplier = source.minimumControlMultiplier;
    }

    public ZoomSettings snapshot() { return new ZoomSettings(this); }
    public float referenceFov() { return referenceFov; }
    public float[] magnificationLevels() { return magnificationLevels.clone(); }
    public float minimumFov() { return minimumFov; }
    public float maximumFov() { return maximumFov; }
    public long transitionDurationMillis() { return transitionDurationMillis; }
    public boolean hideHeldItem() { return hideHeldItem; }
    public boolean displayReticle() { return displayReticle; }
    public boolean scaleControlsWithZoom() { return scaleControlsWithZoom; }
    public float minimumControlMultiplier() { return minimumControlMultiplier; }

    void setReferenceFov(float value) { referenceFov = value; }
    void setMagnificationLevels(float[] value) { magnificationLevels = value == null ? null : value.clone(); }
    void setMinimumFov(float value) { minimumFov = value; }
    void setMaximumFov(float value) { maximumFov = value; }
    void setTransitionDurationMillis(long value) { transitionDurationMillis = value; }
    void setHideHeldItem(boolean value) { hideHeldItem = value; }
    void setDisplayReticle(boolean value) { displayReticle = value; }
    void setScaleControlsWithZoom(boolean value) { scaleControlsWithZoom = value; }
    void setMinimumControlMultiplier(float value) { minimumControlMultiplier = value; }
}
