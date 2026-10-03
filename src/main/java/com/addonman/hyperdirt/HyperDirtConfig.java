package com.addonman.hyperdirt;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HyperDirtConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue IMPACT_FRAMES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        IMPACT_FRAMES = b.comment("Enable the 200ms kill impact frames").define("impact_frames", true);
        SPEC = b.build();
    }

    private HyperDirtConfig() {}
}
