package com.saloeater.structure_analyzer.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class LocatedStructureRenderer  implements IIngredientRenderer<LocatedStructureIngredient> {
    private final IIngredientHelper<LocatedStructureIngredient> ingredientHelper;

    public LocatedStructureRenderer(IIngredientHelper<LocatedStructureIngredient> ingredientHelper) {
        this.ingredientHelper = ingredientHelper;
    }

    @Override
    public void render(GuiGraphics guiGraphics, LocatedStructureIngredient locatedStructureIngredient) {
        ItemStack itemStack;
        if (locatedStructureIngredient.isStructureType()) {
            // Structures: Oak/Birch doors
            if (locatedStructureIngredient.isEvenRow()) {
                itemStack = new ItemStack(Blocks.OAK_DOOR);
            } else {
                itemStack = new ItemStack(Blocks.BIRCH_DOOR);
            }
        } else {
            // Biomes: Grass/Dirt
            if (locatedStructureIngredient.isEvenRow()) {
                itemStack = new ItemStack(Blocks.GRASS_BLOCK);
            } else {
                itemStack = new ItemStack(Blocks.DIRT);
            }
        }
        guiGraphics.renderFakeItem(itemStack, 0, 0);
    }

    @Override
    public List<Component> getTooltip(LocatedStructureIngredient locatedStructureIngredient, TooltipFlag tooltipFlag) {
        String displayName = this.ingredientHelper.getDisplayName(locatedStructureIngredient);
        return List.of(Component.translatable(displayName));
    }
}
