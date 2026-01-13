package com.saloeater.structure_analyzer.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class LocatedStructureHelper  implements IIngredientHelper<LocatedStructureIngredient> {
    @Override
    public IIngredientType<LocatedStructureIngredient> getIngredientType() {
        return LocatedStructureIngredient.TYPE;
    }

    @Override
    public String getDisplayName(LocatedStructureIngredient locatedStructureIngredient) {
        return locatedStructureIngredient.structureName().toString();
    }

    @Override
    public String getUniqueId(LocatedStructureIngredient locatedStructureIngredient, UidContext uidContext) {
        return getIngredientType().getUid() + "_" + locatedStructureIngredient.structureName().toString();
    }

    @Override
    public ResourceLocation getResourceLocation(LocatedStructureIngredient locatedStructureIngredient) {
        return new ResourceLocation(locatedStructureIngredient.structureName().getNamespace(), "structure");
    }

    @Override
    public LocatedStructureIngredient copyIngredient(LocatedStructureIngredient locatedStructureIngredient) {
        return locatedStructureIngredient.copy();
    }

    @Override
    public String getErrorInfo(@Nullable LocatedStructureIngredient locatedStructureIngredient) {
        return locatedStructureIngredient == null ? "located structure ingredient: null" : getDisplayName(locatedStructureIngredient);
    }

    @Override
    public String getDisplayModId(LocatedStructureIngredient ingredient) {
        return ingredient.structureName().getNamespace();
    }
}
