package com.author.blank_mixin_mod.mixin;

import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipeType;
import com.author.blank_mixin_mod.util.JEIHackStorage;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.recipes.InternalRecipeManagerPlugin;
import mezz.jei.library.recipes.collect.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = InternalRecipeManagerPlugin.class, remap = false)
public abstract class InternalRecipeManagerPluginMixin {
    @Inject(method = "getRecipeTypes", at = @At("RETURN"), cancellable = true)
    private <V> void blank_mixin_mod_log$GetRecipeTypes(IFocus<V> focus, CallbackInfoReturnable<List<RecipeType<?>>> cir) {
        if (!JEIHackStorage.shouldAddRecipeType) {
            return;
        }

        var list = new ArrayList<>(cir.getReturnValue());
        list.add(LocateStructureRecipeType.INSTANCE);
        cir.setReturnValue(list);
    }
}
