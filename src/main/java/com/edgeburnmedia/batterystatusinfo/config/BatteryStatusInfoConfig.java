package com.edgeburnmedia.batterystatusinfo.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Replaces the original Fabric build's Cloth-Config/AutoConfig + ModMenu setup.
 * Forge's built-in ForgeConfigSpec already gives players an in-game config
 * screen (via a mods-list "Config" button) with no extra dependency needed.
 */
public class BatteryStatusInfoConfig {

    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue SHOW_HUD;
    public static final ForgeConfigSpec.BooleanValue SHOW_HUD_WHEN_FULLY_CHARGED;
    public static final ForgeConfigSpec.EnumValue<Position> POSITION;
    public static final ForgeConfigSpec.DoubleValue HUD_ICON_SCALE;

    public static final ForgeConfigSpec.BooleanValue SHOW_LOW_BATTERY_ALERT;
    public static final ForgeConfigSpec.BooleanValue SHOW_FULLY_CHARGED_ALERT;
    public static final ForgeConfigSpec.BooleanValue SHOW_CHARGING_ALERT;
    public static final ForgeConfigSpec.BooleanValue SHOW_DISCHARGING_ALERT;
    public static final ForgeConfigSpec.IntValue LOW_BATTERY_THRESHOLD;

    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hud");
        SHOW_HUD = builder
                .comment("Show the battery icon on the HUD")
                .define("showHud", true);
        SHOW_HUD_WHEN_FULLY_CHARGED = builder
                .comment("Keep showing the HUD icon once the battery is fully charged")
                .define("showHudWhenFullyCharged", true);
        POSITION = builder
                .comment("Where to draw the battery icon on screen")
                .defineEnum("position", Position.TOP_RIGHT);
        HUD_ICON_SCALE = builder
                .comment("Scale of the HUD battery icon")
                .defineInRange("hudIconScale", 1.0, 0.25, 4.0);
        builder.pop();

        builder.push("alerts");
        SHOW_LOW_BATTERY_ALERT = builder
                .comment("Show a toast alert when the battery is low")
                .define("showLowBatteryAlert", true);
        SHOW_FULLY_CHARGED_ALERT = builder
                .comment("Show a toast alert when the battery finishes charging")
                .define("showFullyChargedAlert", true);
        SHOW_CHARGING_ALERT = builder
                .comment("Show a toast alert when charging begins")
                .define("showChargingAlert", true);
        SHOW_DISCHARGING_ALERT = builder
                .comment("Show a toast alert when charging stops. NOTICE: on some systems "
                        + "this may fire right after reaching 100% charge.")
                .define("showDischargingAlert", true);
        LOW_BATTERY_THRESHOLD = builder
                .comment("Battery percentage at/below which the low-battery alert fires")
                .defineInRange("lowBatteryThreshold", 20, 1, 99);
        builder.pop();

        builder.push("general");
        CHECK_INTERVAL = builder
                .comment("How often to check the battery status, in milliseconds")
                .defineInRange("checkInterval", 60000, 1000, 600000);
        builder.pop();

        SPEC = builder.build();
    }
}
