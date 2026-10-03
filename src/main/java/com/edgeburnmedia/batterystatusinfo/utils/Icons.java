package com.edgeburnmedia.batterystatusinfo.utils;

import net.minecraft.resources.ResourceLocation;

public class Icons {

    private static ResourceLocation of(String name) {
        return new ResourceLocation("batterystatusinfo", "textures/gui/" + name + ".png");
    }

    public static final ResourceLocation BATTERY_0 = of("battery_0");
    public static final ResourceLocation BATTERY_25 = of("battery_25");
    public static final ResourceLocation BATTERY_50 = of("battery_50");
    public static final ResourceLocation BATTERY_75 = of("battery_75");
    public static final ResourceLocation BATTERY_FULL = of("battery_full");
    public static final ResourceLocation BATTERY_UNKNOWN = of("battery_unknown");

    public static final ResourceLocation BATTERY_0_CHARGING = of("battery_0_charging");
    public static final ResourceLocation BATTERY_25_CHARGING = of("battery_25_charging");
    public static final ResourceLocation BATTERY_50_CHARGING = of("battery_50_charging");
    public static final ResourceLocation BATTERY_75_CHARGING = of("battery_75_charging");
    public static final ResourceLocation BATTERY_FULL_CHARGING = of("battery_full_charging");
    public static final ResourceLocation BATTERY_UNKNOWN_CHARGING = of("battery_unknown_charging");

    /** Picks the right icon variant for a percentage + charging state. */
    public static ResourceLocation forStatus(int percent, boolean charging, boolean present) {
        if (!present) {
            return charging ? BATTERY_UNKNOWN_CHARGING : BATTERY_UNKNOWN;
        }
        if (percent >= 100) {
            return charging ? BATTERY_FULL_CHARGING : BATTERY_FULL;
        }
        if (percent >= 75) {
            return charging ? BATTERY_75_CHARGING : BATTERY_75;
        }
        if (percent >= 50) {
            return charging ? BATTERY_50_CHARGING : BATTERY_50;
        }
        if (percent >= 25) {
            return charging ? BATTERY_25_CHARGING : BATTERY_25;
        }
        return charging ? BATTERY_0_CHARGING : BATTERY_0;
    }
}
