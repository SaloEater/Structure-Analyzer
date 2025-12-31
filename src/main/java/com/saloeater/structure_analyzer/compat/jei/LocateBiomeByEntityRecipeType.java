package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import mezz.jei.api.recipe.RecipeType;

public class LocateBiomeByEntityRecipeType {
    public static final RecipeType<LocateBiomeByEntityRecipe> INSTANCE =
            RecipeType.create(StructureAnalyzer.MODID, "locate_biome_by_entity", LocateBiomeByEntityRecipe.class);
}
