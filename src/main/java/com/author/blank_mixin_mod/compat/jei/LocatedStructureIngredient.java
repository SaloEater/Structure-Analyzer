package com.author.blank_mixin_mod.compat.jei;

import mezz.jei.api.ingredients.IIngredientType;

public record LocatedStructureIngredient(String structureName, boolean isEvenRow) {
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
