package com.author.blank_mixin_mod.mixin;

import com.author.blank_mixin_mod.util.JEIHackStorage;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RecipesGui.class, remap = false)
public abstract class RecipesGuiMixin {
    @Shadow
    private void updateLayout(){}

    @Inject(
            method = "show",
            at = @At("HEAD")
    )
    private void blank_mixin_mod_log$ShowRecipesGui(List<IFocus<?>> focuses, CallbackInfo ci) {
        JEIHackStorage.shouldAddRecipeType = true;
    }

    @Inject(
            method = "show",
            at = @At("RETURN")
    )
    private void blank_mixin_mod_log$ResetShowRecipesGui(List<IFocus<?>> focuses, CallbackInfo ci) {
        JEIHackStorage.shouldAddRecipeType = false;
    }

    @Inject(
            method="render",
            at = @At("HEAD")
    )
    private void blank_mixin_mod_log$RenderRecipesGui(CallbackInfo ci) {
        if (!JEIHackStorage.shouldUpdateLayout) {
            return;
        }

        this.updateLayout();
        JEIHackStorage.shouldUpdateLayout = false;
    }
}
