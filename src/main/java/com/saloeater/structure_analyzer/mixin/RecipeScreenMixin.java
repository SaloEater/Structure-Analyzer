package com.saloeater.structure_analyzer.mixin;

import com.google.common.collect.Lists;
import com.saloeater.structure_analyzer.compat.emi.LocateStructureCategory;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipeScreen.class, remap = false)
public abstract class RecipeScreenMixin {
    @Shadow
    private List<RecipeTab> tabs = Lists.newArrayList();

    @Shadow
    private int tab;

    @Inject(
            method="mouseScrolled",//"mouseScrolled",
            at = @At("RETURN")
    )
    private void structure_analyzer$OnMouseScrolled(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        RecipeTab tab = tabs.get(this.tab);
        var category = tab.category;
        if (!(category instanceof LocateStructureCategory locateStructureCategory)) {
            return;
        }

        locateStructureCategory.HandleMouseScroll(mouseX, mouseY, delta);
    }

    @Inject(
            method="mouseDragged",//"mouseDragged",
            at = @At("RETURN")
    )
    private void structure_analyzer$OnMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        RecipeTab tab = tabs.get(this.tab);
        var category = tab.category;
        if (!(category instanceof LocateStructureCategory locateStructureCategory)) {
            return;
        }

        locateStructureCategory.HandleMouseDragged(mouseX, mouseY, deltaX, deltaY);
    }

    @Inject(
            method="mouseClicked",//"mouseClicked",
            at = @At("RETURN")
    )
    private void structure_analyzer$OnMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        RecipeTab tab = tabs.get(this.tab);
        var category = tab.category;
        if (!(category instanceof LocateStructureCategory locateStructureCategory)) {
            return;
        }

        locateStructureCategory.HandleMouseClicked(mouseX, mouseY);
    }

    @Inject(
            method="mouseReleased",//"mouseReleased",
            at = @At("RETURN")
    )
    private void structure_analyzer$OnMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        RecipeTab tab = tabs.get(this.tab);
        var category = tab.category;
        if (!(category instanceof LocateStructureCategory locateStructureCategory)) {
            return;
        }

        locateStructureCategory.HandleMouseReleased();
    }


}
