package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class SpyglassConfig {
    public static final BuilderCodec<SpyglassConfig> CODEC = BuilderCodec.builder(SpyglassConfig.class, SpyglassConfig::new)
            .addField(new KeyedCodec<>("Zoom", ZoomSettings.CODEC), SpyglassConfig::setZoom, SpyglassConfig::zoom)
            .addField(new KeyedCodec<>("Drops", DropSettings.CODEC), SpyglassConfig::setDrops, SpyglassConfig::drops)
            .validator(ConfigValidator::validateForCodec)
            .build();

    private ZoomSettings zoom = new ZoomSettings();
    private DropSettings drops = new DropSettings();

    public ZoomSettings zoom() { return zoom; }
    void setZoom(ZoomSettings value) { zoom = value; }
    public DropSettings drops() { return drops; }
    void setDrops(DropSettings value) { drops = value; }
}
