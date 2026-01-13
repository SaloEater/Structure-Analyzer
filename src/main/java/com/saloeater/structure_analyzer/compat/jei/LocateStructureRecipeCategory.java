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

public class LocateStructureRecipeCategory extends AbstractLocateRecipeCategory<LocateStructureRecipe> {
    private static LocateStructureRecipe currentRecipe;

    public LocateStructureRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(Blocks.OAK_DOOR));
    }

    @Override
    public RecipeType<LocateStructureRecipe> getRecipeType() {
        return LocateStructureRecipeType.INSTANCE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("structure_analyzer.jei.locate_structure.title");
    }

    @Override
    protected ItemStack getItemStack(LocateStructureRecipe recipe) {
        return recipe.itemStack;
    }

    @Override
    protected void setCurrentRecipe(LocateStructureRecipe recipe) {
        currentRecipe = recipe;
    }

    @Override
    protected SearchRequest getSearchRequest() {
        if (currentRecipe == null || currentRecipe.itemStack == null) {
            return new SearchRequest(null, null, SearchRequest.TYPE_STRUCTURE);
        }

        if (currentRecipe.itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(spawnEggItem.getType(null));
            return new SearchRequest(null, entityId, SearchRequest.TYPE_STRUCTURE);
        }

        // Assume it's a block item
        var block = net.minecraft.world.level.block.Block.byItem(currentRecipe.itemStack.getItem());
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        return new SearchRequest(blockId, null, SearchRequest.TYPE_STRUCTURE);
    }

    @Override
    protected String getResultsPrefixKey() {
        return "structure_analyzer.jei.results_found.prefix";
    }

    @Override
    protected String getResultsSuffixKey() {
        return "structure_analyzer.jei.results_found.suffix";
    }
}
