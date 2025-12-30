package com.saloeater.structure_analyzer;

import com.mojang.logging.LogUtils;
import com.saloeater.structure_analyzer.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(StructureAnalyzer.MODID)
public class StructureAnalyzer
{
    public static final String MODID = "structure_analyzer";
    private static final Logger LOGGER = LogUtils.getLogger();

    public StructureAnalyzer()
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
