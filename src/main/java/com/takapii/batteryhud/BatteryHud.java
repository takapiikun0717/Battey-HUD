package com.takapii.batteryhud;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(BatteryHud.MODID)
public class BatteryHud {
    public static final String MODID = "batteryhud";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BatteryHud() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC);
    }
}
