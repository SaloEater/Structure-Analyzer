package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.network.SearchRequest;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class LocateBiomeByEntityRecipeCategory extends AbstractLocateRecipeCategory<LocateBiomeByEntityRecipe> {
    private static LocateBiomeByEntityRecipe currentRecipe;

    public LocateBiomeByEntityRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(Blocks.GRASS_BLOCK));
    }

    @Override
    public RecipeType<LocateBiomeByEntityRecipe> getRecipeType() {
        return LocateBiomeByEntityRecipeType.INSTANCE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("structure_analyzer.jei.locate_biome_by_entity.title");
    }

    @Override
    protected ItemStack getItemStack(LocateBiomeByEntityRecipe recipe) {
        return recipe.itemStack;
    }

    @Override
    protected void setCurrentRecipe(LocateBiomeByEntityRecipe recipe) {
        currentRecipe = recipe;
    }

    @Override
    protected SearchRequest getSearchRequest() {
        if (currentRecipe == null || currentRecipe.itemStack == null) {
            return new SearchRequest(null, null, SearchRequest.TYPE_BIOME);
        }

        if (currentRecipe.itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(spawnEggItem.getType(null));
            return new SearchRequest(null, entityId, SearchRequest.TYPE_BIOME);
        }
        return new SearchRequest(null, null, SearchRequest.TYPE_BIOME);
    }

    @Override
    protected String getResultsPrefixKey() {
        return "structure_analyzer.jei.biomes_found.prefix";
    }

    @Override
    protected String getResultsSuffixKey() {
        return "structure_analyzer.jei.biomes_found.suffix";
    }

    @Override
    protected int getSearchType() {
        return SearchRequest.TYPE_BIOME;
    }
}
