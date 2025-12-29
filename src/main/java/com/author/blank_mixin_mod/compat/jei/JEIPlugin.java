package com.author.blank_mixin_mod.compat.jei;

import com.author.blank_mixin_mod.BlankMixinMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BlankMixinMod.MODID, "plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // Register treasure bag category
        registration.addRecipeCategories(
                new LocateStructureRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(LocateStructureRecipeType.INSTANCE, List.of(new LocateStructureRecipe(null)));
    }
}
