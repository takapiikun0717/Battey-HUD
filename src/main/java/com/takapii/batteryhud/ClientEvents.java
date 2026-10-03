package com.takapii.batteryhud;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final KeyMapping TOGGLE = new KeyMapping(
            "key.batteryhud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.batteryhud");
    private static boolean visible = true;

    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = BatteryHud.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent e) {
            BatteryReader.start();
        }

        @SubscribeEvent
        public static void onKeys(RegisterKeyMappingsEvent e) {
            e.register(TOGGLE);
        }

        @SubscribeEvent
        public static void onOverlays(RegisterGuiOverlaysEvent e) {
            e.registerAboveAll("battery", ClientEvents::render);
        }
    }

    @Mod.EventBusSubscriber(modid = BatteryHud.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            while (TOGGLE.consumeClick()) visible = !visible;
        }
    }

    private static int colorFor(BatteryReader.Status s) {
        if (s.charging()) return 0xFF55FFFF;
        if (s.percent() <= 20) return 0xFFFF5555;
        if (s.percent() <= 50) return 0xFFFFFF55;
        return 0xFF55FF55;
    }

    private static String formatTime(long secs) {
        long m = secs / 60;
        return (m / 60) + ":" + String.format("%02d", m % 60);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !visible || !HudConfig.ENABLED.get()) return;

        BatteryReader.Status s = BatteryReader.get();
        if (!s.present() && HudConfig.HIDE_WHEN_NO_BATTERY.get()) return;

        Font font = mc.font;
        int color = s.present() ? colorFor(s) : 0xFFAAAAAA;

        StringBuilder sb = new StringBuilder(s.present() ? s.percent() + "%" : "--%");
        if (s.present()) {
            if (s.charging()) {
                sb.append(' ').append(Component.translatable("batteryhud.charging").getString());
            } else if (s.acOnline()) {
                sb.append(' ').append(Component.translatable("batteryhud.plugged").getString());
            } else if (HudConfig.SHOW_TIME_REMAINING.get() && s.secondsLeft() > 0) {
                sb.append(' ').append(formatTime(s.secondsLeft()));
            }
        }
        String text = sb.toString();

        float sc = HudConfig.SCALE.get().floatValue();
        int totalW = 24 + font.width(text);
        int w = (int) Math.ceil(totalW * sc);
        int h = (int) Math.ceil(9 * sc);
        int ox = HudConfig.OFFSET_X.get();
        int oy = HudConfig.OFFSET_Y.get();
        HudConfig.Anchor a = HudConfig.ANCHOR.get();
        int x = (a == HudConfig.Anchor.TOP_LEFT || a == HudConfig.Anchor.BOTTOM_LEFT) ? ox : sw - w - ox;
        int y = (a == HudConfig.Anchor.TOP_LEFT || a == HudConfig.Anchor.TOP_RIGHT) ? oy : sh - h - oy;

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(sc, sc, 1f);

        // battery icon: 18x9 body + 2x5 tip
        g.fill(0, 0, 18, 9, 0xFFE0E0E0);
        g.fill(1, 1, 17, 8, 0xFF202020);
        int fillW = s.present() ? Math.max(s.percent() > 0 ? 1 : 0, Math.round(16 * s.percent() / 100f)) : 0;
        if (fillW > 0) g.fill(1, 1, 1 + fillW, 8, color);
        g.fill(18, 2, 20, 7, 0xFFE0E0E0);

        g.drawString(font, text, 24, 1, color, true);
        g.pose().popPose();
    }
}
