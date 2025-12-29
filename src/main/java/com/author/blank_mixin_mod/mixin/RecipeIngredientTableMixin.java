package com.author.blank_mixin_mod.mixin;

import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipe;
import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipeType;
import com.author.blank_mixin_mod.util.JEIHackStorage;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.recipes.collect.RecipeIngredientTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RecipeIngredientTable.class, remap = false)
public abstract class RecipeIngredientTableMixin {
    @Inject(
            method = "get",
            at = @At("RETURN"),
            cancellable = true
    )
    private <V> void blank_mixin_mod$Get(RecipeType<V> recipeType, String ingredientUid, CallbackInfoReturnable<List<V>> cir) {
        if (!JEIHackStorage.shouldAddRecipeType || recipeType != LocateStructureRecipeType.INSTANCE) {
            return;
        }

        var modifiableList = new ArrayList<>(cir.getReturnValue());
        modifiableList.add((V) new LocateStructureRecipe(null));
        cir.setReturnValue(modifiableList);
    }
}
