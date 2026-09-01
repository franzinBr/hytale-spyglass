package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/** Drop configuration decoded at plugin preload time. */
public final class DropSettings {
    public static final BuilderCodec<DropSettings> CODEC = BuilderCodec.builder(DropSettings.class, DropSettings::new)
            .addField(new KeyedCodec<>("SkeletonPirates", SkeletonPirateDropSettings.CODEC),
                    DropSettings::setSkeletonPirates, DropSettings::skeletonPirates)
            .build();

    private SkeletonPirateDropSettings skeletonPirates = new SkeletonPirateDropSettings();

    public SkeletonPirateDropSettings skeletonPirates() { return skeletonPirates; }
    void setSkeletonPirates(SkeletonPirateDropSettings value) { skeletonPirates = value; }
}
