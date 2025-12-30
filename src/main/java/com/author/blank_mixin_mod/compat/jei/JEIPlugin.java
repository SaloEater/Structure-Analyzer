package com.author.blank_mixin_mod.compat.jei;

import com.author.blank_mixin_mod.BlankMixinMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.library.plugins.debug.DebugSimpleRecipeManagerPlugin;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredient;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredientHelper;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredientRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(LocateStructureRecipeType.INSTANCE, new LocateStructureRecipeManagerPlugin());
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        var ingredientHelper = new LocatedStructureHelper();
        var ingredientRenderer = new LocatedStructureRenderer(ingredientHelper);
        registration.register(LocatedStructureIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
    }
}
