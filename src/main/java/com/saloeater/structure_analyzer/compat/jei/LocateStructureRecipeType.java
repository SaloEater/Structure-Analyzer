package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import mezz.jei.api.recipe.RecipeType;

public class LocateStructureRecipeType {
    public static final RecipeType<LocateStructureRecipe> INSTANCE =
            RecipeType.create(StructureAnalyzer.MODID, "locate_structure", LocateStructureRecipe.class);
}
