package com.takapii.batteryhud;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HudConfig {
    public enum Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum Source { AUTO, OSHI, NATIVE }

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<Anchor> ANCHOR;
    public static final ForgeConfigSpec.IntValue OFFSET_X;
    public static final ForgeConfigSpec.IntValue OFFSET_Y;
    public static final ForgeConfigSpec.DoubleValue SCALE;
    public static final ForgeConfigSpec.BooleanValue SHOW_TIME_REMAINING;
    public static final ForgeConfigSpec.BooleanValue HIDE_WHEN_NO_BATTERY;
    public static final ForgeConfigSpec.IntValue REFRESH_SECONDS;
    public static final ForgeConfigSpec.EnumValue<Source> SOURCE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOG;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        ENABLED = b.comment("Show the battery HUD").define("enabled", true);
        ANCHOR = b.comment("Screen corner to anchor the HUD to").defineEnum("anchor", Anchor.TOP_LEFT);
        OFFSET_X = b.comment("Horizontal distance from the anchored corner (px)").defineInRange("offsetX", 4, 0, 4000);
        OFFSET_Y = b.comment("Vertical distance from the anchored corner (px)").defineInRange("offsetY", 4, 0, 4000);
        SCALE = b.comment("HUD scale").defineInRange("scale", 1.0, 0.5, 4.0);
        SHOW_TIME_REMAINING = b.comment("Show estimated time remaining while on battery").define("showTimeRemaining", false);
        HIDE_WHEN_NO_BATTERY = b.comment("Hide the HUD on PCs without a battery (desktops)").define("hideWhenNoBattery", true);
        REFRESH_SECONDS = b.comment("How often to poll the battery (seconds)").defineInRange("refreshSeconds", 5, 1, 300);
        SOURCE = b.comment("Where to read the battery from. AUTO = OS native first (PowerShell/WMI on Windows, /sys on Linux), OSHI as fallback")
                .defineEnum("source", Source.AUTO);
        DEBUG_LOG = b.comment("Log raw battery values (OSHI and native) every poll to latest.log").define("debugLog", false);
        SPEC = b.build();
    }

    private HudConfig() {}
}
