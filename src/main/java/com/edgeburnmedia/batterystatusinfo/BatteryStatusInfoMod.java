package com.edgeburnmedia.batterystatusinfo;

import com.edgeburnmedia.batterystatusinfo.config.BatteryStatusInfoConfig;
import com.edgeburnmedia.batterystatusinfo.gui.BatteryHud;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Forge port of the "batterystatusinfo" Fabric mod (client-only), targeting
 * Minecraft 1.20.1 / Forge 47.4.10.
 *
 * On Fabric this mod split its behaviour across three entrypoints:
 *  - "main"    -> BatteryStatusInfoMod        (common init)
 *  - "client"  -> BatteryStatusInfoModClient  (HUD render callback, thread start)
 *  - "modmenu" -> BatteryStatusInfoModMenuIntegration (ModMenu config screen)
 *
 * On Forge that collapses into this single @Mod class: config now lives in
 * config/batterystatusinfo-client.toml via ForgeConfigSpec (edit the file, or
 * add a screen library like "Configured" for an in-game GUI - vanilla Forge
 * 1.20.1 doesn't auto-generate one), the HUD hooks into
 * RegisterGuiOverlaysEvent instead of Fabric API's HudRenderCallback, and the
 * polling thread is started/stopped in FMLClientSetupEvent / on client
 * shutdown instead of Fabric's client lifecycle events.
 */
@Mod(BatteryStatusInfoMod.MOD_ID)
public class BatteryStatusInfoMod {

    public static final String MOD_ID = "batterystatusinfo";

    private BatteryCheckerThread checkerThread;

    public BatteryStatusInfoMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerOverlays);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BatteryStatusInfoConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Nothing server-relevant: this mod is client-only (see mods.toml side = "CLIENT").
    }

    private void clientSetup(FMLClientSetupEvent event) {
        checkerThread = new BatteryCheckerThread();
        checkerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (checkerThread != null) {
                checkerThread.shutdown();
            }
        }));
    }

    private void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                new ResourceLocation(MOD_ID, "battery_hud").getPath(),
                BatteryHud.INSTANCE
        );
    }
}
