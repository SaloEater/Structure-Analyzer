package com.saloeater.structure_analyzer.compat.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public class LocateStructureRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<LocateStructureRecipe> {

    @Override
    public boolean isHandledInput(ITypedIngredient<?> iTypedIngredient) {
        return false;
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> iTypedIngredient) {
        Optional<ItemStack> oItemStack = iTypedIngredient.getItemStack();
        if (oItemStack.isEmpty()) {
            return false;
        }

        var itemStack = oItemStack.get();
        return itemStack.getItem() instanceof BlockItem block && ForgeRegistries.BLOCKS.containsValue(block.getBlock()) || itemStack.getItem() instanceof SpawnEggItem;
    }

    @Override
    public List<LocateStructureRecipe> getRecipesForInput(ITypedIngredient<?> iTypedIngredient) {
        return List.of();
    }

    @Override
    public List<LocateStructureRecipe> getRecipesForOutput(ITypedIngredient<?> iTypedIngredient) {
        Optional<ItemStack> oItemStack = iTypedIngredient.getItemStack();
        if (oItemStack.isEmpty()) {
            return List.of();
        }

        var itemStack = oItemStack.get();
        return List.of(new LocateStructureRecipe(itemStack));
    }

    @Override
    public List<LocateStructureRecipe> getAllRecipes() {
        return List.of(new LocateStructureRecipe(new ItemStack(Items.AIR)));
    }
}
