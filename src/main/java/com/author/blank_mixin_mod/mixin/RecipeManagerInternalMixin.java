package com.author.blank_mixin_mod.mixin;

import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipeType;
import com.author.blank_mixin_mod.util.JEIHackStorage;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.recipes.RecipeManagerInternal;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(value = RecipeManagerInternal.class, remap = false)
public abstract class RecipeManagerInternalMixin {
    @Shadow
    private RecipeTypeDataMap recipeTypeDataMap;

    private IFocusGroup focuses;

    @Inject(
            method="getRecipeCategoriesForTypes",
            at= @At("HEAD")
    )
    private void blank_mixin_mod_init$JEIHackStorage(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden, CallbackInfoReturnable<Stream<IRecipeCategory<?>>> cir) {
        this.focuses = focuses;
    }

    @ModifyVariable(
            method="getRecipeCategoriesForTypes",
            name = "recipeCategories",
            at= @At(value = "INVOKE", target = "Lmezz/jei/library/recipes/RecipeManagerInternal;getRecipeCategoriesCached(Ljava/util/Collection;Lmezz/jei/api/recipe/IFocusGroup;Z)Ljava/util/stream/Stream;")
    )
    private List<IRecipeCategory<?>> blank_mixin_mod_log$RecipeCategoriesForTypes(List<IRecipeCategory<?>> recipeCategories) {
        ItemStack searchStack = ItemStack.EMPTY;
        Optional<IFocus<ItemStack>> first = this.focuses.getItemStackFocuses().findFirst();
        if (first.isEmpty()) {
            return recipeCategories;
        }
        searchStack = first.get().getTypedValue().getIngredient();

        List<IRecipeCategory<?>> modifiableList = new ArrayList<>(recipeCategories);
        var recipeTypeData = this.recipeTypeDataMap.get(LocateStructureRecipeType.INSTANCE);
        modifiableList.add(recipeTypeData.getRecipeCategory());
        return modifiableList;
    }

    @Inject(
            method="getRecipeCategoriesForTypes",
            at= @At("RETURN")
    )
    private void blank_mixin_mod_reset$JEIHackStorage(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden, CallbackInfoReturnable<Stream<IRecipeCategory<?>>> cir) {
        this.focuses = null;
    }
}
