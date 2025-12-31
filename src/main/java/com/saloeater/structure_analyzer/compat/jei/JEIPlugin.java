package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(StructureAnalyzer.MODID, "plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // Register locate structure category
        registration.addRecipeCategories(
                new LocateStructureRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );

        // Register locate biome by entity category
        registration.addRecipeCategories(
                new LocateBiomeByEntityRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(LocateStructureRecipeType.INSTANCE, List.of(new LocateStructureRecipe(new ItemStack(Items.AIR))));
        registration.addRecipes(LocateBiomeByEntityRecipeType.INSTANCE, List.of(new LocateBiomeByEntityRecipe(new ItemStack(Items.AIR))));
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(LocateStructureRecipeType.INSTANCE, new LocateStructureRecipeManagerPlugin());
        registration.addTypedRecipeManagerPlugin(LocateBiomeByEntityRecipeType.INSTANCE, new LocateBiomeByEntityRecipeManagerPlugin());
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        var ingredientHelper = new LocatedStructureHelper();
        var ingredientRenderer = new LocatedStructureRenderer(ingredientHelper);
        registration.register(LocatedStructureIngredient.TYPE, Collections.emptyList(), ingredientHelper, ingredientRenderer);
    }
}
