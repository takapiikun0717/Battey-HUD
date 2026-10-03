package com.edgeburnmedia.batterystatusinfo;

import com.edgeburnmedia.batterystatusinfo.config.BatteryStatusInfoConfig;
import com.edgeburnmedia.batterystatusinfo.toast.BatteryAlertToast;
import net.minecraft.client.Minecraft;

/**
 * Polls OSHI on a background daemon thread (battery reads can block briefly
 * on some platforms) and hands the latest reading to the HUD, scheduling any
 * toast alerts back onto the main render thread.
 */
public class BatteryCheckerThread extends Thread {

    private static volatile BatteryStatus latest = BatteryStatus.UNKNOWN;

    private final BatteryMonitor monitor = new BatteryMonitor();
    private volatile boolean running = true;

    public BatteryCheckerThread() {
        super("BatteryStatusInfo-Checker");
        setDaemon(true);
    }

    public static BatteryStatus getLatest() {
        return latest;
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        BatteryStatus previous = BatteryStatus.UNKNOWN;

        while (running) {
            BatteryStatus current = monitor.read();
            latest = current;

            handleTransition(previous, current);
            previous = current;

            try {
                Thread.sleep(Math.max(1000, BatteryStatusInfoConfig.CHECK_INTERVAL.get()));
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    private void handleTransition(BatteryStatus previous, BatteryStatus current) {
        if (!current.isPresent()) {
            return;
        }

        int threshold = BatteryStatusInfoConfig.LOW_BATTERY_THRESHOLD.get();

        if (BatteryStatusInfoConfig.SHOW_LOW_BATTERY_ALERT.get()
                && current.isLow(threshold) && !previous.isLow(threshold)) {
            queueToast(BatteryAlertToast.Kind.LOW_BATTERY, current);
        }

        if (BatteryStatusInfoConfig.SHOW_FULLY_CHARGED_ALERT.get()
                && current.isFullyCharged() && !previous.isFullyCharged()) {
            queueToast(BatteryAlertToast.Kind.FULLY_CHARGED, current);
        }

        if (BatteryStatusInfoConfig.SHOW_CHARGING_ALERT.get()
                && current.isCharging() && !previous.isCharging()) {
            queueToast(BatteryAlertToast.Kind.CHARGING, current);
        }

        if (BatteryStatusInfoConfig.SHOW_DISCHARGING_ALERT.get()
                && !current.isCharging() && previous.isCharging()) {
            queueToast(BatteryAlertToast.Kind.DISCHARGING, current);
        }
    }

    private void queueToast(BatteryAlertToast.Kind kind, BatteryStatus status) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.getToasts().addToast(new BatteryAlertToast(kind, status.getPercent())));
    }
}
