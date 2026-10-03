package com.edgeburnmedia.batterystatusinfo;

/**
 * Immutable snapshot of the host machine's battery at a point in time.
 */
public class BatteryStatus {

    public static final BatteryStatus UNKNOWN = new BatteryStatus(-1, false, false);

    private final int percent;
    private final boolean charging;
    private final boolean present;

    public BatteryStatus(int percent, boolean charging, boolean present) {
        this.percent = percent;
        this.charging = charging;
        this.present = present;
    }

    public int getPercent() {
        return percent;
    }

    public boolean isCharging() {
        return charging;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isFullyCharged() {
        return present && percent >= 100;
    }

    public boolean isLow(int thresholdPercent) {
        return present && !charging && percent <= thresholdPercent;
    }

    @Override
    public String toString() {
        return "BatteryStatus{percent=" + percent + ", charging=" + charging + ", present=" + present + "}";
    }
}
