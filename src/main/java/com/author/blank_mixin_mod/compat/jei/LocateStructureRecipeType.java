package com.author.blank_mixin_mod.compat.jei;

import com.author.blank_mixin_mod.BlankMixinMod;
import mezz.jei.api.recipe.RecipeType;

public class LocateStructureRecipeType {
    public static final RecipeType<LocateStructureRecipe> INSTANCE =
            RecipeType.create(BlankMixinMod.MODID, "locate_structure", LocateStructureRecipe.class);
}
