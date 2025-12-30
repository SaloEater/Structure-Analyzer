package com.saloeater.structure_analyzer.mixin;

import com.saloeater.structure_analyzer.util.JEIHackStorage;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipesGui.class, remap = false)
public abstract class RecipesGuiMixin {
    @Shadow
    private void updateLayout(){}

    @Inject(
            method="render",
            at = @At("HEAD")
    )
    private void structure_analyzer_log$RenderRecipesGui(CallbackInfo ci) {
        if (!JEIHackStorage.shouldUpdateLayout) {
            return;
        }

        JEIHackStorage.shouldResetLayout = true;
        this.updateLayout();
        JEIHackStorage.shouldUpdateLayout = false;
    }
}
