package com.author.blank_mixin_mod.mixin;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.gui.recipes.FocusedRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FocusedRecipes.class, remap = false)
public interface FocusedRecipesAccessor {
    @Accessor("focuses")
    IFocusGroup getFocuses();
}
