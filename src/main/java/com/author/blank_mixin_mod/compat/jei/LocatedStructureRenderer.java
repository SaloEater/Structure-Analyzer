package com.author.blank_mixin_mod.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        if (locatedStructureIngredient.isEvenRow()) {
            itemStack = new ItemStack(Blocks.OAK_DOOR);
        } else {
            itemStack = new ItemStack(Blocks.BIRCH_DOOR);
        }
        guiGraphics.renderFakeItem(itemStack, 0, 0);
    }

    @Override
    public List<Component> getTooltip(LocatedStructureIngredient locatedStructureIngredient, TooltipFlag tooltipFlag) {
        JeiTooltip tooltip = new JeiTooltip();
        String displayName = this.ingredientHelper.getDisplayName(locatedStructureIngredient);
        tooltip.add(Component.literal(displayName));
        return tooltip.toLegacyToComponents();
    }
}
