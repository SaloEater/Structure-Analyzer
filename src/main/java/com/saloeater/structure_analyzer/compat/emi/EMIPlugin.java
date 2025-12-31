package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {
    public static final LocateStructureCategory LOCATE_STRUCTURE_CATEGORY = new LocateStructureCategory(
            new ResourceLocation(StructureAnalyzer.MODID, "locate_structure"),
            EmiStack.of(Blocks.OAK_DOOR)
    );public static final LocateStructureCategory LOCATE_BIOME_CATEGORY = new LocateStructureCategory(
            new ResourceLocation(StructureAnalyzer.MODID, "locate_biome"),
            EmiStack.of(Blocks.GRASS_BLOCK)
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(LOCATE_STRUCTURE_CATEGORY);
        registry.addCategory(LOCATE_BIOME_CATEGORY);

        var blocksEntries = ForgeRegistries.BLOCKS.getEntries();
        for (var blockEntry : blocksEntries) {
            registry.addRecipe(new LocateStructureByBlockRecipe(blockEntry));
        }

        var entityEntries = ForgeRegistries.ENTITY_TYPES.getEntries();
        for (var entityEntry : entityEntries) {
            registry.addRecipe(new LocateStructureByEntityRecipe(entityEntry));
            registry.addRecipe(new LocateBiomeByEntityRecipe(entityEntry));
        }
    }
}
