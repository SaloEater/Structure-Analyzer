package com.author.blank_mixin_mod.compat.jei;

import com.author.blank_mixin_mod.network.NetworkHandler;
import com.author.blank_mixin_mod.network.StartSearchC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.GuiIconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class LocateStructureRecipeCategory implements IRecipeCategory<LocateStructureRecipe> {
    private final IDrawable background;
    private final IGuiHelper guiHelper;
    private IDrawable icon;
    private GuiIconButton searchButton;
    private static LocateStructureRecipe Recipe;

    public LocateStructureRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(180, 130);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.OAK_DOOR));
        this.searchButton = new GuiIconButton(new DrawableBlank(0, 0), b -> LocateStructureRecipeCategory.StartSearch(), Internal.getTextures());
        this.searchButton.updateBounds(new ImmutableRect2i(50, 50, 80, 20));
    }

    @Override
    public RecipeType<LocateStructureRecipe> getRecipeType() {
        return LocateStructureRecipeType.INSTANCE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("structure_analyzer.jei.locate_structure.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LocateStructureRecipe recipe, IFocusGroup focuses) {
        LocateStructureRecipeCategory.Recipe = recipe;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);

        if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty()) {
            int x = 10;
            int y = 40;
            int slotSize = 18;
            int slotsPerRow = 9;

            for (int i = 0; i < state.foundStructures.size(); i++) {
                String structureName = state.foundStructures.get(i);

                int slotX = x + (i % slotsPerRow) * slotSize;
                int slotY = y + (i / slotsPerRow) * slotSize;

                ItemStack signStack = new ItemStack(Items.OAK_SIGN);
                signStack.setHoverName(Component.literal(structureName).withStyle(ChatFormatting.GOLD));

                builder.addSlot(RecipeIngredientRole.OUTPUT, slotX, slotY)
                    .addItemStack(signStack)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.clear();
                        tooltip.add(Component.literal(structureName).withStyle(ChatFormatting.GOLD));
                    });
            }
        }
    }

    @Override
    public void draw(LocateStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        if (state.state == ClientSearchState.SearchState.NOT_STARTED) {
            searchButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
            guiGraphics.drawCenteredString(font, "Start search", 90, 56, ChatFormatting.WHITE.getColor());
        } else if (state.state == ClientSearchState.SearchState.IN_PROGRESS) {
            drawProgressBar(guiGraphics, font, state, 10, 50, 160, 20);
        } else if (state.state == ClientSearchState.SearchState.COMPLETED) {
            if (state.foundStructures.isEmpty()) {
                String message = "No structures found";
                int messageWidth = font.width(message);
                guiGraphics.drawString(font, message, (background.getWidth() - messageWidth) / 2, 55,
                    ChatFormatting.RED.getColor(), false);
            } else {
                String message = "Found " + state.foundStructures.size() + " structure(s)";
                guiGraphics.drawString(font, message, 10, 25, ChatFormatting.GREEN.getColor(), false);
            }
        }
    }

    @Override
    public boolean handleInput(LocateStructureRecipe recipe, double mouseX, double mouseY, InputConstants.Key inputn) {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        if (isMouseOverButton(mouseX, mouseY, 50, 50, 80, 20) && state.state == ClientSearchState.SearchState.NOT_STARTED) {
            searchButton.onPress();
            return true;
        }

        return false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }


    private void drawProgressBar(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                 ClientSearchState.RecipeSearchState state, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF000000);

        if (state.searchTotal > 0) {
            int progressWidth = (int) ((state.searchCurrent / (float) state.searchTotal) * (width - 2));
            guiGraphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + height - 1, 0xFF00AA00);
        }

        String progressText = state.searchCurrent + " / " + state.searchTotal;
        int textWidth = font.width(progressText);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - font.lineHeight) / 2;

        guiGraphics.drawString(font, progressText, textX, textY, 0xFFFFFF, false);
    }

    private static void StartSearch() {
        LocateStructureRecipe recipe = LocateStructureRecipeCategory.Recipe;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        if (state.state != ClientSearchState.SearchState.NOT_STARTED) {
            return;
        }

        ClientSearchState.startSearch(recipe.itemStack.getDescriptionId());
        NetworkHandler.sendToServer(new StartSearchC2SPacket(recipe.itemStack.getDescriptionId()));

    }
}
