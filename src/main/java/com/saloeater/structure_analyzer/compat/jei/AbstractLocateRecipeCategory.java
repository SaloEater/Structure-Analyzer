package com.saloeater.structure_analyzer.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.saloeater.structure_analyzer.network.SearchRequest;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class AbstractLocateRecipeCategory<T> implements IRecipeCategory<T> {
    // Textures
    protected static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("textures/gui/slider.png");
    protected static final int SLIDER_TEXTURE_WIDTH = 200;
    protected static final int SLIDER_TEXTURE_HEIGHT = 20;
    protected static final int SLIDER_BACKGROUND_UV_Y = 0;
    protected static final int SLIDER_FILL_UV_Y = 40;
    protected static final int SLIDER_BORDER_SIZE = 1;

    // Background dimensions
    protected static final int BACKGROUND_WIDTH = 180;
    protected static final int BACKGROUND_HEIGHT = 127;

    // Header
    protected static final int HEADER_HEIGHT = 18;
    protected static int HEADER_ITEM_SLOT_X = 11;
    protected static final int HEADER_ITEM_SLOT_Y = 0;
    protected static final int HEADER_RESULTS_Y = 4;
    protected static final int SLOT_SIZE = 16;
    protected static int HEADER_WIDTH = 158;

    // Search button
    protected static final int SEARCH_BUTTON_WIDTH = 80;
    protected static final int SEARCH_BUTTON_HEIGHT = 20;
    protected static final int SEARCH_BUTTON_X = 50;
    protected static final int SEARCH_BUTTON_Y = 50;

    // Progress bar
    protected static final int PROGRESS_BAR_X = 10;
    protected static final int PROGRESS_BAR_Y = 50;
    protected static final int PROGRESS_BAR_WIDTH = 160;
    protected static final int PROGRESS_BAR_HEIGHT = 20;

    // Result slots
    protected static final int HEADER_PADDING = 1;
    protected static final int RESULT_GRID_Y = HEADER_HEIGHT + HEADER_PADDING;
    protected static final int RESULT_GRID_HEIGHT = BACKGROUND_HEIGHT - RESULT_GRID_Y;

    // Text positions
    protected static final int NO_RESULTS_TEXT_Y = 55;

    // Colors
    protected static final int COLOR_PROGRESS_TEXT = 0xFFFFFF;
    protected static final int COLOR_HEADER_SEPARATOR = 0xFF8B8B8B;
    protected static final int gridColumns = 8;

    // Instance fields
    protected final IDrawable background;
    protected final IGuiHelper guiHelper;
    protected final IDrawable icon;
    protected final Button searchButton;
    protected String lastRecipeId = null;

    // Calculated values
    protected final int searchButtonTextX;
    protected final int searchButtonTextY;

    protected AbstractLocateRecipeCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(iconStack);

        // Create search button
        this.searchButton = Button.builder(Component.translatable("structure_analyzer.jei.button.start_search"), button -> this.startSearch()).bounds(SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT)
            .build();

        // Calculate text position (centered in button)
        this.searchButtonTextX = SEARCH_BUTTON_X + SEARCH_BUTTON_WIDTH / 2;
        this.searchButtonTextY = SEARCH_BUTTON_Y + (SEARCH_BUTTON_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2;
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
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        ItemStack itemStack = getItemStack(recipe);
        setCurrentRecipe(recipe);

        String recipeId = "";
        if (itemStack != null) {
            recipeId = itemStack.getDescriptionId();
        }

        if (!recipeId.equals(lastRecipeId)) {
            lastRecipeId = recipeId;
        }

        // Add header slot for the input item (always visible)
        builder.addSlot(RecipeIngredientRole.OUTPUT, HEADER_ITEM_SLOT_X, HEADER_ITEM_SLOT_Y)
                .addItemStack(itemStack);

        ClientSearchState.RecipeSearchState state = getSearchRecipeState();
        if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty()) {
            boolean isEvenRow = true;
            var i = 0;
            for (var structureName : state.foundStructures) {
                builder.addInputSlot().addIngredient(LocatedStructureIngredient.TYPE, new LocatedStructureIngredient(structureName, isEvenRow));
                i++;
                if (i % gridColumns == 0) {
                    isEvenRow = !isEvenRow;
                }
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        ClientSearchState.RecipeSearchState state = getSearchRecipeState();

        if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty()) {
            // Get all output slots
            IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
            List<IRecipeSlotDrawable> outputSlots = recipeSlots.getSlots(RecipeIngredientRole.INPUT);

            // Create scroll grid widget (10 columns, 6 rows)
            IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(outputSlots, gridColumns, 6);
            scrollGridWidget.setPosition(0, RESULT_GRID_Y, BACKGROUND_WIDTH, RESULT_GRID_HEIGHT,
                    HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            HEADER_WIDTH = scrollGridWidget.getScreenRectangle().width() - 2;

            var inputSlot = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT).get(0);
            HEADER_ITEM_SLOT_X = scrollGridWidget.getScreenRectangle().position().x() + 1;
            inputSlot.setPosition( HEADER_ITEM_SLOT_X, HEADER_ITEM_SLOT_Y);
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ClientSearchState.RecipeSearchState state = getSearchRecipeState();

        guiGraphics.fill(
                RenderType.gui(),
                HEADER_ITEM_SLOT_X,
                HEADER_ITEM_SLOT_Y,
                HEADER_ITEM_SLOT_X + HEADER_WIDTH,
                SLOT_SIZE,
                0x30000000
        );

        if (state.state == ClientSearchState.SearchState.NOT_STARTED) {
            searchButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
        } else if (state.state == ClientSearchState.SearchState.IN_PROGRESS) {
            drawProgressBar(guiGraphics, font, state);
        } else if (state.state == ClientSearchState.SearchState.COMPLETED) {
            if (state.foundStructures.isEmpty()) {
                String message = Component.translatable("structure_analyzer.jei.no_results").getString();
                int messageWidth = font.width(message);
                int messageX = (BACKGROUND_WIDTH - messageWidth) / 2;
                guiGraphics.drawString(font, message, messageX, NO_RESULTS_TEXT_Y, ChatFormatting.DARK_RED.getColor(), true);
            } else {
                // Draw "Found X structures/biomes" in 3 colors
                String prefix = Component.translatable(getResultsPrefixKey()).getString();
                String count = String.valueOf(state.foundStructures.size());
                String suffix = Component.translatable(getResultsSuffixKey()).getString();

                int prefixWidth = font.width(prefix);
                int countWidth = font.width(count);
                int suffixWidth = font.width(suffix);
                int spaceWidth = font.width(" ");
                int totalWidth = prefixWidth + spaceWidth + countWidth + spaceWidth + suffixWidth;

                int startX = (BACKGROUND_WIDTH - totalWidth) / 2;
                int currentX = startX;

                // Draw "Found" in white
                guiGraphics.drawString(font, prefix, currentX, HEADER_RESULTS_Y, ChatFormatting.WHITE.getColor(), true);
                currentX += prefixWidth + spaceWidth;

                // Draw count in gold
                guiGraphics.drawString(font, count, currentX, HEADER_RESULTS_Y, ChatFormatting.GREEN.getColor(), true);
                currentX += countWidth + spaceWidth;

                // Draw "structures/biomes" in white
                guiGraphics.drawString(font, suffix, currentX, HEADER_RESULTS_Y, ChatFormatting.WHITE.getColor(), true);
            }
        }
    }

    @Override
    public boolean handleInput(T recipe, double mouseX, double mouseY, InputConstants.Key input) {
        ClientSearchState.RecipeSearchState state = getSearchRecipeState();

        if (state.state == ClientSearchState.SearchState.NOT_STARTED && input.getValue() == InputConstants.MOUSE_BUTTON_LEFT) {
            if (isMouseOverButton(mouseX, mouseY, SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT)) {
                searchButton.onPress();
                return true;
            }
        }

        return false;
    }

    protected boolean isMouseOverButton(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected void drawProgressBar(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                 ClientSearchState.RecipeSearchState state) {
        // Draw background texture
        guiGraphics.blitNineSliced(SLIDER_TEXTURE, PROGRESS_BAR_X, PROGRESS_BAR_Y,
                PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
                SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE,
                SLIDER_TEXTURE_WIDTH, SLIDER_TEXTURE_HEIGHT, 0, SLIDER_BACKGROUND_UV_Y);

        // Draw progress fill
        if (state.searchTotal > 0) {
            int progressWidth = (int) ((state.searchCurrent / (float) state.searchTotal) * PROGRESS_BAR_WIDTH);
            if (progressWidth > 0) {
                guiGraphics.blitNineSliced(SLIDER_TEXTURE, PROGRESS_BAR_X, PROGRESS_BAR_Y,
                        progressWidth, PROGRESS_BAR_HEIGHT,
                        SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE, SLIDER_BORDER_SIZE,
                        SLIDER_TEXTURE_WIDTH, SLIDER_TEXTURE_HEIGHT,
                        0, SLIDER_FILL_UV_Y);
            }
        }

        // Draw progress text
        String progressText = Component.translatable("structure_analyzer.jei.progress", state.searchCurrent, state.searchTotal).getString();
        int textWidth = font.width(progressText);
        int textX = PROGRESS_BAR_X + (PROGRESS_BAR_WIDTH - textWidth) / 2;
        int textY = PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - font.lineHeight) / 2;

        guiGraphics.drawString(font, progressText, textX, textY, COLOR_PROGRESS_TEXT, true);
    }

    protected ClientSearchState.RecipeSearchState getSearchRecipeState() {
        var searchRequest = getSearchRequest();
        return ClientSearchState.getSearchState(searchRequest);
    }

    protected void startSearch() {
        var request = getSearchRequest();
        ClientSearchState.startSearch(request);
        ClientSearchManager.startSearch(request);
    }

    // Abstract methods to be implemented by subclasses
    protected abstract ItemStack getItemStack(T recipe);
    protected abstract void setCurrentRecipe(T recipe);
    protected abstract SearchRequest getSearchRequest();
    protected abstract String getResultsPrefixKey();
    protected abstract String getResultsSuffixKey();
}
