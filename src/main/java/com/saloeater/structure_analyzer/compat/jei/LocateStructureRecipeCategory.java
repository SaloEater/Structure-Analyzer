package com.saloeater.structure_analyzer.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.saloeater.structure_analyzer.network.NetworkHandler;
import com.saloeater.structure_analyzer.network.SearchRequest;
import com.saloeater.structure_analyzer.network.StartSearchC2SPacket;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class LocateStructureRecipeCategory implements IRecipeCategory<LocateStructureRecipe> {
    // Textures
    private static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("textures/gui/slider.png");
    private static final int SLIDER_TEXTURE_WIDTH = 200;
    private static final int SLIDER_TEXTURE_HEIGHT = 20;
    private static final int SLIDER_BACKGROUND_UV_Y = 0;
    private static final int SLIDER_FILL_UV_Y = 40;
    private static final int SLIDER_BORDER_SIZE = 1;

    // Background dimensions
    private static final int BACKGROUND_WIDTH = 180;
    private static final int BACKGROUND_HEIGHT = 127;

    // Header
    private static final int HEADER_HEIGHT = 18;
    private static int HEADER_ITEM_SLOT_X = 11;
    private static final int HEADER_ITEM_SLOT_Y = 0;
    private static final int HEADER_RESULTS_Y = 4;
    private static final int SLOT_SIZE = 16;
    private static int HEADER_WIDTH = 158;

    // Search button
    private static final int SEARCH_BUTTON_WIDTH = 80;
    private static final int SEARCH_BUTTON_HEIGHT = 20;
    private static final int SEARCH_BUTTON_X = 50;
    private static final int SEARCH_BUTTON_Y = 50;

    // Progress bar
    private static final int PROGRESS_BAR_X = 10;
    private static final int PROGRESS_BAR_Y = 50;
    private static final int PROGRESS_BAR_WIDTH = 160;
    private static final int PROGRESS_BAR_HEIGHT = 20;

    // Result slots
    private static final int HEADER_PADDING = 1;
    private static final int RESULT_GRID_Y = HEADER_HEIGHT + HEADER_PADDING;
    private static final int RESULT_GRID_HEIGHT = BACKGROUND_HEIGHT - RESULT_GRID_Y;

    // Text positions
    private static final int NO_RESULTS_TEXT_Y = 55;

    // Colors
    private static final int COLOR_PROGRESS_TEXT = 0xFFFFFF;
    private static final int COLOR_HEADER_SEPARATOR = 0xFF8B8B8B;
    public static final int gridColumns = 8;

    // Instance fields
    private final IDrawable background;
    private final IGuiHelper guiHelper;
    private final IDrawable icon;
    private final Button searchButton;
    private static LocateStructureRecipe Recipe;
    private static String lastRecipeId = null;

    // Calculated values
    private final int searchButtonTextX;
    private final int searchButtonTextY;

    public LocateStructureRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.OAK_DOOR));

        // Create search button
        this.searchButton = Button.builder(Component.translatable("structure_analyzer.jei.button.start_search"), button -> LocateStructureRecipeCategory.StartSearch()).bounds(SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT)
            .build();

        // Calculate text position (centered in button)
        this.searchButtonTextX = SEARCH_BUTTON_X + SEARCH_BUTTON_WIDTH / 2;
        this.searchButtonTextY = SEARCH_BUTTON_Y + (SEARCH_BUTTON_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2;
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
        String recipeId = "";
        if (recipe.itemStack != null) {
            recipeId = recipe.itemStack.getDescriptionId();
        }

        if (!recipeId.equals(lastRecipeId)) {
            lastRecipeId = recipeId;
        }

        // Add header slot for the input item (always visible)
        builder.addSlot(RecipeIngredientRole.OUTPUT, HEADER_ITEM_SLOT_X, HEADER_ITEM_SLOT_Y)
                .addItemStack(recipe.itemStack);

        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBlock(recipeId);
        if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty()) {
            boolean isEvenRow = true;
            var i = 0;
            for (String structureName : state.foundStructures) {
                builder.addInputSlot().addIngredient(LocatedStructureIngredient.TYPE, new LocatedStructureIngredient(structureName, isEvenRow));
                i++;
                if (i % gridColumns == 0) {
                    isEvenRow = !isEvenRow;
                }
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, LocateStructureRecipe recipe, IFocusGroup focuses) {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBlock(recipe.itemStack.getDescriptionId());

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
    public void draw(LocateStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBlock(recipe.itemStack.getDescriptionId());

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
                // Draw "Found X structures" in 3 colors
                String prefix = Component.translatable("structure_analyzer.jei.results_found.prefix").getString();
                String count = String.valueOf(state.foundStructures.size());
                String suffix = Component.translatable("structure_analyzer.jei.results_found.suffix").getString();

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

                // Draw "structures" in white
                guiGraphics.drawString(font, suffix, currentX, HEADER_RESULTS_Y, ChatFormatting.WHITE.getColor(), true);
            }
        }
    }

    @Override
    public boolean handleInput(LocateStructureRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBlock(recipe.itemStack.getDescriptionId());

        if (state.state == ClientSearchState.SearchState.NOT_STARTED && input.getValue() == InputConstants.MOUSE_BUTTON_LEFT) {
            if (isMouseOverButton(mouseX, mouseY, SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT)) {
                searchButton.onPress();
                return true;
            }
        }

        return false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawProgressBar(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
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

    private static void StartSearch() {
        LocateStructureRecipe recipe = LocateStructureRecipeCategory.Recipe;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBlock(recipe.itemStack.getDescriptionId());

        if (state.state != ClientSearchState.SearchState.NOT_STARTED) {
            return;
        }

        ClientSearchState.startSearch(recipe.itemStack.getDescriptionId());
        NetworkHandler.sendToServer(new StartSearchC2SPacket(new SearchRequest(recipe.itemStack.getDescriptionId(), "")));
    }
}
