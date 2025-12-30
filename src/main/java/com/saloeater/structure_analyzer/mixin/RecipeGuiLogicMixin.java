package com.saloeater.structure_analyzer.mixin;

import com.saloeater.structure_analyzer.util.JEIHackStorage;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipeGuiLogic.class, remap = false)
public abstract class RecipeGuiLogicMixin {
    @Shadow
    private @Nullable IRecipeLayoutList cachedRecipeLayoutsWithButtons;

    @Inject(
            method = "getVisibleRecipeLayoutsWithButtons",
            at = @At("HEAD")
    )
    private void structure_analyzer_log$GetVisibleRecipeLayoutsWithButtons(int availableHeight, int minRecipePadding, @Nullable AbstractContainerMenu container, CallbackInfoReturnable<List<RecipeLayoutWithButtons<?>>> cir) {
        if (JEIHackStorage.shouldResetLayout) {
            this.cachedRecipeLayoutsWithButtons = null;
            JEIHackStorage.shouldResetLayout = false;
        }
    }
}
