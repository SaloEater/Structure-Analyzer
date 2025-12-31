package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.util.EMIHack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.resources.ResourceLocation;

public class LocateStructureCategory extends EmiRecipeCategory {
    private double scrollMouseX = 0;
    private double scrollMouseY = 0;
    private double scrollDelta = 0;

    private double dragMouseX = 0;
    private double dragMouseY = 0;
    private double dragDeltaX = 0;
    private double dragDeltaY = 0;
    private double clickMouseX;
    private double clickMouseY;
    private boolean mouseIsClicked;

    public LocateStructureCategory(ResourceLocation id, EmiRenderable icon) {
        super(id, icon);
    }

    public void HandleMouseScroll(double mouseX, double mouseY, double delta) {
        this.scrollMouseX = mouseX;
        this.scrollMouseY = mouseY;
        this.scrollDelta = delta;
        EMIHack.reloadEMIScreen();
    }

    public double getScrollDelta() {
        var old = scrollDelta;
        scrollDelta = 0;
        return old;
    }

    public double getScrollMouseX() {
        var old = scrollMouseX;
        scrollMouseX = 0;
        return old;
    }

    public double getScrollMouseY() {
        var old = scrollMouseY;
        scrollMouseY = 0;
        return old;
    }

    public void HandleMouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.dragMouseX = mouseX;
        this.dragMouseY = mouseY;
        this.dragDeltaX = deltaX;
        this.dragDeltaY = deltaY;
        EMIHack.reloadEMIScreen();
    }

    public double getDragDeltaX() {
        var old = dragDeltaX;
        dragDeltaX = 0;
        return old;
    }

    public double getDragDeltaY() {
        var old = dragDeltaY;
        dragDeltaY = 0;
        return old;
    }

    public double getDragMouseX() {
        var old = dragMouseX;
        dragMouseX = 0;
        return old;
    }

    public double getDragMouseY() {
        var old = dragMouseY;
        dragMouseY = 0;
        return old;
    }

    public void HandleMouseClicked(double mouseX, double mouseY) {
        this.clickMouseX = mouseX;
        this.clickMouseY = mouseY;
        this.mouseIsClicked = true;
        EMIHack.reloadEMIScreen();
    }

    public double getClickMouseX() {
        var old = clickMouseX;
        clickMouseX = 0;
        return old;
    }

    public double getClickMouseY() {
        var old = clickMouseY;
        clickMouseY = 0;
        return old;
    }

    public boolean isMouseClicked() {
        return mouseIsClicked;
    }

    public void HandleMouseReleased() {
        this.mouseIsClicked = false;
        EMIHack.reloadEMIScreen();
    }
}
