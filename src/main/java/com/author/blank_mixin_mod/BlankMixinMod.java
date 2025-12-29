package com.author.blank_mixin_mod;

import com.author.blank_mixin_mod.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BlankMixinMod.MODID)
public class BlankMixinMod
{
    public static final String MODID = "blank_mixin_mod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BlankMixinMod()
    {
        var forgeBus = MinecraftForge.EVENT_BUS;
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);
        forgeBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("Network handler registered");
    }
}
