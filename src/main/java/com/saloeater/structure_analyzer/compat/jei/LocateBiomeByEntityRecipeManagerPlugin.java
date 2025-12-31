package com.saloeater.structure_analyzer.compat.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;
import java.util.Optional;

public class LocateBiomeByEntityRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<LocateBiomeByEntityRecipe> {

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
        return itemStack.getItem() instanceof SpawnEggItem;
    }

    @Override
    public List<LocateBiomeByEntityRecipe> getRecipesForInput(ITypedIngredient<?> iTypedIngredient) {
        return List.of();
    }

    @Override
    public List<LocateBiomeByEntityRecipe> getRecipesForOutput(ITypedIngredient<?> iTypedIngredient) {
        Optional<ItemStack> oItemStack = iTypedIngredient.getItemStack();
        if (oItemStack.isEmpty()) {
            return List.of();
        }

        var itemStack = oItemStack.get();
        if (itemStack.getItem() instanceof SpawnEggItem) {
            return List.of(new LocateBiomeByEntityRecipe(itemStack));
        }
        return List.of();
    }

    @Override
    public List<LocateBiomeByEntityRecipe> getAllRecipes() {
        return List.of(new LocateBiomeByEntityRecipe(new ItemStack(Items.AIR)));
    }
}
