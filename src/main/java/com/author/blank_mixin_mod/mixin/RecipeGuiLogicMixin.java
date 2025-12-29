package com.author.blank_mixin_mod.mixin;

import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipe;
import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipeCategory;
import com.author.blank_mixin_mod.compat.jei.LocateStructureRecipeType;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.ingredients.IngredientLookupState;
import mezz.jei.gui.recipes.FocusedRecipes;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = RecipeGuiLogic.class, remap = false)
public abstract class RecipeGuiLogicMixin {
    @Shadow
    private IngredientLookupState state;

    @Shadow
    private IRecipeManager recipeManager;

    @Inject(
            method="getRecipeLayouts()Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void blank_mixin_mod_init$JEIHackStorage(CallbackInfoReturnable<List<IRecipeLayoutDrawable<?>>> cir) {
        var selectedRecipes = this.state.getFocusedRecipes();
        var recipeType = selectedRecipes.getRecipeCategory().getRecipeType();
        if (!recipeType.equals(LocateStructureRecipeType.INSTANCE)) {
            return;
        }

        var focuses = ((FocusedRecipesAccessor) selectedRecipes).getFocuses();
        Optional<IFocus<ItemStack>> first = focuses.getItemStackFocuses().findFirst();
        if (first.isEmpty()) {
            return;
        }

        ItemStack searchStack = first.get().getTypedValue().getIngredient();
        LocateStructureRecipe recipe = new LocateStructureRecipe(searchStack);

        IRecipeCategory<LocateStructureRecipe> recipeCategory = (IRecipeCategory<LocateStructureRecipe>) this.state.getFocusedRecipes().getRecipeCategory();

        var modifiableList = new ArrayList<>(cir.getReturnValue());

        recipeManager.createRecipeLayoutDrawable(recipeCategory, recipe, state.getFocuses())
                .ifPresentOrElse(modifiableList::add, () -> {});

        cir.setReturnValue(modifiableList);
    }

    /*@ModifyVariable(
            method="getRecipeLayouts(Lmezz/jei/gui/recipes/FocusedRecipes;)Ljava/util/List;",
            name = "recipes",
            at = @At(value = "INVOKE", target = "Lmezz/jei/gui/recipes/FocusedRecipes;getRecipes()Ljava/util/List;", shift = At.Shift.AFTER)
    )
    private <T> List<T> blank_mixin_mod_log$ModifyGetRecipeLayouts(List<T> recipes) {
        var selectedRecipes = this.state.getFocusedRecipes();
        var recipeType = selectedRecipes.getRecipeCategory().getRecipeType();
        if (!recipeType.equals(LocateStructureRecipeType.INSTANCE)) {
            return recipes;
        }

        var focuses = ((FocusedRecipesAccessor) selectedRecipes).getFocuses();
        Optional<IFocus<ItemStack>> first = focuses.getItemStackFocuses().findFirst();
        if (first.isEmpty()) {
            return recipes;
        }

        ItemStack searchStack = first.get().getTypedValue().getIngredient();
        var modifiableList = new ArrayList<>(recipes);
        modifiableList.add((T) new LocateStructureRecipe(searchStack.getDescriptionId()));
        return modifiableList;
    }*/
}
