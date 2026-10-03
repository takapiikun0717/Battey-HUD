package com.edgeburnmedia.batterystatusinfo;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

import java.util.List;

/**
 * Reads the current battery/power-source state via OSHI, the same library
 * the original Fabric build shaded in through fabric.mod.json's "jars" entry.
 */
public class BatteryMonitor {

    private final SystemInfo systemInfo = new SystemInfo();

    public BatteryStatus read() {
        try {
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            List<PowerSource> sources = hal.getPowerSources();
            if (sources.isEmpty()) {
                return BatteryStatus.UNKNOWN;
            }

            // Desktops without a battery are reported by OSHI as an empty list
            // (or a source stuck at -1/-2); treat those as "no battery present".
            PowerSource source = sources.get(0);
            double fraction = source.getRemainingCapacityPercent();
            if (fraction < 0) {
                return BatteryStatus.UNKNOWN;
            }

            int percent = (int) Math.round(fraction * 100.0);
            boolean charging = source.isCharging() || source.isPowerOnLine();
            return new BatteryStatus(percent, charging, true);
        } catch (Exception e) {
            return BatteryStatus.UNKNOWN;
        }
    }
}
