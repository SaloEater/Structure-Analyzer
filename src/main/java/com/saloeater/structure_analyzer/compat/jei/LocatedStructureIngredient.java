package com.saloeater.structure_analyzer.compat.jei;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

public record LocatedStructureIngredient(ResourceLocation structureName, boolean isEvenRow) {
    public static final IIngredientType<LocatedStructureIngredient> TYPE = new IIngredientType<>() {
        @Override
        public String getUid() {
            return "located_structure";
        }

        @Override
        public Class<? extends LocatedStructureIngredient> getIngredientClass() {
            return LocatedStructureIngredient.class;
        }
    };

    public LocatedStructureIngredient copy() {
        return new LocatedStructureIngredient(structureName, isEvenRow);
    }
}
