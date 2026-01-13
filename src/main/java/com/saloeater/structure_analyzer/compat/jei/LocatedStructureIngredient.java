package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.network.SearchRequest;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

public record LocatedStructureIngredient(ResourceLocation structureName, boolean isEvenRow, int type) {
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
        return new LocatedStructureIngredient(structureName, isEvenRow, type);
    }

    public boolean isStructureType() {
        return type == SearchRequest.TYPE_STRUCTURE;
    }

    public boolean isBiomeType() {
        return type == SearchRequest.TYPE_BIOME;
    }
}
