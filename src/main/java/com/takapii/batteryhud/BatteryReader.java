package com.takapii.batteryhud;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Polls the battery on a background thread so the render thread never blocks on OS calls. */
public final class BatteryReader {
    public record Status(boolean present, int percent, boolean charging, boolean acOnline, long secondsLeft) {
        public static final Status NONE = new Status(false, 0, false, false, -1);
    }

    private static volatile Status current = Status.NONE;
    private static Thread thread;
    private static boolean nativeWarned = false;

    private BatteryReader() {}

    public static Status get() {
        return current;
    }

    public static synchronized void start() {
        if (thread != null) return;
        thread = new Thread(BatteryReader::loop, "BatteryHud-Reader");
        thread.setDaemon(true);
        thread.start();
    }

    private static void loop() {
        HardwareAbstractionLayer hal = null;
        try {
            hal = new SystemInfo().getHardware();
        } catch (Throwable t) {
            BatteryHud.LOGGER.warn("[batteryhud] OSHI unavailable, native reader only", t);
        }
        boolean warned = false;
        while (true) {
            HudConfig.Source src = HudConfig.Source.AUTO;
            boolean debug = false;
            int sec = 5;
            try {
                src = HudConfig.SOURCE.get();
                debug = HudConfig.DEBUG_LOG.get();
                sec = HudConfig.REFRESH_SECONDS.get();
            } catch (Exception ignored) {
            }
            try {
                Status oshi = null, nat = null;
                if (src != HudConfig.Source.NATIVE && hal != null) oshi = readOshi(hal);
                if (src != HudConfig.Source.OSHI || debug) nat = readNative();
                if (debug) {
                    BatteryHud.LOGGER.info("[batteryhud] oshi={} native={} source={}", oshi, nat, src);
                }
                Status result;
                if (src == HudConfig.Source.OSHI) result = oshi != null ? oshi : Status.NONE;
                else if (src == HudConfig.Source.NATIVE) result = nat != null ? nat : Status.NONE;
                else result = nat != null ? nat : (oshi != null ? oshi : Status.NONE);
                current = result;
            } catch (Throwable t) {
                current = Status.NONE;
                if (!warned) {
                    BatteryHud.LOGGER.warn("[batteryhud] Could not read battery status", t);
                    warned = true;
                }
            }
            try {
                Thread.sleep(Math.max(1, sec) * 1000L);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    // ---------- OSHI ----------
    private static Status readOshi(HardwareAbstractionLayer hal) {
        List<PowerSource> sources = hal.getPowerSources();
        if (sources == null || sources.isEmpty()) return Status.NONE;
        PowerSource ps = sources.get(0);
        double cap = ps.getRemainingCapacityPercent();
        if (cap < 0) return Status.NONE;
        int pct = (int) Math.round(Math.min(1.0, cap) * 100.0);
        double t = ps.getTimeRemainingEstimated();
        long secs = t > 0 ? (long) t : -1;
        return new Status(true, pct, ps.isCharging(), ps.isPowerOnLine(), secs);
    }

    // ---------- Native ----------
    private static Status readNative() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) return readWindows();
            if (os.contains("linux")) return readLinux();
        } catch (Throwable t) {
            if (!nativeWarned) {
                nativeWarned = true;
                BatteryHud.LOGGER.warn("[batteryhud] native battery read failed (falling back to OSHI)", t);
            }
        }
        return null; // unsupported OS -> fall back to OSHI
    }

    private static Status readWindows() throws Exception {
        String ps = "$b = Get-CimInstance Win32_Battery | Select-Object -First 1; "
                + "if ($b) { ($b.EstimatedChargeRemaining, $b.BatteryStatus, $b.EstimatedRunTime) -join ',' } else { 'NONE' }";
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", ps);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String line;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("powershell timed out");
        }
        if (line == null) throw new IllegalStateException("powershell returned no output");
        line = line.trim();
        if (line.equals("NONE")) return Status.NONE;
        String[] parts = line.split(",");
        if (parts.length < 2) throw new IllegalStateException("unexpected powershell output: " + line);
        int pct = Math.max(0, Math.min(100, Integer.parseInt(parts[0].trim())));
        int st = Integer.parseInt(parts[1].trim());
        boolean charging = st >= 6 && st <= 9;
        boolean ac = st == 2 || st == 3 || st == 11 || charging;
        long secs = -1;
        if (parts.length >= 3 && !ac) {
            try {
                long min = Long.parseLong(parts[2].trim());
                if (min > 0 && min < 10000) secs = min * 60;
            } catch (NumberFormatException ignored) {
            }
        }
        return new Status(true, pct, charging, ac, secs);
    }

    private static Status readLinux() throws Exception {
        Path base = Paths.get("/sys/class/power_supply");
        if (!Files.isDirectory(base)) return null;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(base, "BAT*")) {
            for (Path bat : ds) {
                Path capF = bat.resolve("capacity");
                if (!Files.exists(capF)) continue;
                int pct = Math.max(0, Math.min(100, Integer.parseInt(Files.readString(capF).trim())));
                String st = Files.exists(bat.resolve("status")) ? Files.readString(bat.resolve("status")).trim() : "";
                boolean charging = st.equalsIgnoreCase("Charging");
                boolean ac = charging || st.equalsIgnoreCase("Full") || st.equalsIgnoreCase("Not charging");
                return new Status(true, pct, charging, ac, -1);
            }
        }
        return Status.NONE;
    }
}
