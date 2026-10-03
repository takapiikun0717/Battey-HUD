package com.edgeburnmedia.batterystatusinfo.toast;

import com.edgeburnmedia.batterystatusinfo.utils.Icons;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BatteryAlertToast implements Toast {

    public enum Kind {
        LOW_BATTERY("toast.batterystatusinfo.lowbattery"),
        CHARGING("toast.batterystatusinfo.charging"),
        DISCHARGING("toast.batterystatusinfo.discharging"),
        FULLY_CHARGED("toast.batterystatusinfo.generic");

        final String titleKey;

        Kind(String titleKey) {
            this.titleKey = titleKey;
        }
    }

    // Same background texture vanilla SystemToast uses on 1.20.1; the row at
    // v=0 is the plain 160x32 background frame (no progress bar).
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/toasts.png");

    private static final long DISPLAY_TIME_MS = 5000;

    private final Kind kind;
    private final int percent;

    public BatteryAlertToast(Kind kind, int percent) {
        this.kind = kind;
        this.percent = percent;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        graphics.blit(TEXTURE, 0, 0, 0, 0, width(), height());

        Component title = Component.translatable(kind.titleKey);
        Component subtitle = Component.translatable("toast.batterystatusinfo.status", percent);

        graphics.drawString(toastComponent.getMinecraft().font, title, 20, 7, 0xFF500050, false);
        graphics.drawString(toastComponent.getMinecraft().font, subtitle, 20, 18, 0xFF000000, false);

        ResourceLocation icon = Icons.forStatus(percent, kind == Kind.CHARGING, true);
        RenderSystem.enableBlend();
        graphics.blit(icon, 4, 4, 16, 16, 0, 0, 16, 16, 16, 16);

        return timeSinceLastVisible >= DISPLAY_TIME_MS ? Visibility.HIDE : Visibility.SHOW;
    }
}
