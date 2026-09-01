package dev.franzin.spyglass.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/** Mutable codec model. Runtime users receive the immutable {@link Snapshot}. */
public final class SkeletonPirateDropSettings {
    public static final BuilderCodec<SkeletonPirateDropSettings> CODEC = BuilderCodec.builder(
                    SkeletonPirateDropSettings.class, SkeletonPirateDropSettings::new)
            .addField(new KeyedCodec<>("CaptainChancePercent", Codec.FLOAT),
                    SkeletonPirateDropSettings::setCaptainChancePercent, SkeletonPirateDropSettings::captainChancePercent)
            .addField(new KeyedCodec<>("GunnerChancePercent", Codec.FLOAT),
                    SkeletonPirateDropSettings::setGunnerChancePercent, SkeletonPirateDropSettings::gunnerChancePercent)
            .addField(new KeyedCodec<>("StrikerChancePercent", Codec.FLOAT),
                    SkeletonPirateDropSettings::setStrikerChancePercent, SkeletonPirateDropSettings::strikerChancePercent)
            .build();

    private float captainChancePercent = 8.0f;
    private float gunnerChancePercent = 4.0f;
    private float strikerChancePercent = 2.0f;

    public float captainChancePercent() { return captainChancePercent; }
    public float gunnerChancePercent() { return gunnerChancePercent; }
    public float strikerChancePercent() { return strikerChancePercent; }
    public Snapshot snapshot() { return new Snapshot(captainChancePercent, gunnerChancePercent, strikerChancePercent); }

    void setCaptainChancePercent(float value) { captainChancePercent = value; }
    void setGunnerChancePercent(float value) { gunnerChancePercent = value; }
    void setStrikerChancePercent(float value) { strikerChancePercent = value; }

    public record Snapshot(float captainChancePercent, float gunnerChancePercent, float strikerChancePercent) {}
}
