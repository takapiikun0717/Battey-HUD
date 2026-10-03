package com.edgeburnmedia.batterystatusinfo.gui;

import com.edgeburnmedia.batterystatusinfo.BatteryCheckerThread;
import com.edgeburnmedia.batterystatusinfo.BatteryStatus;
import com.edgeburnmedia.batterystatusinfo.config.BatteryStatusInfoConfig;
import com.edgeburnmedia.batterystatusinfo.utils.Icons;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BatteryHud implements IGuiOverlay {

    public static final BatteryHud INSTANCE = new BatteryHud();

    private static final int ICON_SIZE = 16;
    private static final int MARGIN = 4;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!BatteryStatusInfoConfig.SHOW_HUD.get()) {
            return;
        }

        BatteryStatus status = BatteryCheckerThread.getLatest();
        if (!status.isPresent()) {
            return;
        }
        if (status.isFullyCharged() && !BatteryStatusInfoConfig.SHOW_HUD_WHEN_FULLY_CHARGED.get() && !status.isCharging()) {
            return;
        }

        float scale = (float) BatteryStatusInfoConfig.HUD_ICON_SCALE.get().doubleValue();
        int size = Math.round(ICON_SIZE * scale);

        int[] pos = position(screenWidth, screenHeight, size);

        var icon = Icons.forStatus(status.getPercent(), status.isCharging(), true);

        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.blit(icon, pos[0], pos[1], size, size, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.pose().popPose();
    }

    private int[] position(int screenWidth, int screenHeight, int size) {
        int margin = MARGIN;
        return switch (BatteryStatusInfoConfig.POSITION.get()) {
            case TOP_LEFT -> new int[]{margin, margin};
            case TOP_RIGHT -> new int[]{screenWidth - size - margin, margin};
            case BOTTOM_LEFT -> new int[]{margin, screenHeight - size - margin};
            case BOTTOM_RIGHT -> new int[]{screenWidth - size - margin, screenHeight - size - margin};
        };
    }
}
