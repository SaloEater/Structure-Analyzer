package com.saloeater.structure_analyzer.compat.jei;


import net.minecraft.world.item.ItemStack;

public class LocateStructureRecipe {
    public final ItemStack itemStack;

    public LocateStructureRecipe(ItemStack blockDescriptionId) {
        this.itemStack = blockDescriptionId;
    }
}
