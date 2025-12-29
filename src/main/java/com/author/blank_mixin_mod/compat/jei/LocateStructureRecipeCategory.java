package com.author.blank_mixin_mod.compat.jei;

import com.author.blank_mixin_mod.network.NetworkHandler;
import com.author.blank_mixin_mod.network.StartSearchC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.library.plugins.debug.DebugRecipe;
import mezz.jei.library.plugins.debug.DebugRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class LocateStructureRecipeCategory implements IRecipeCategory<LocateStructureRecipe> {
    // Background dimensions
    private static final int BACKGROUND_WIDTH = 180;
    private static final int BACKGROUND_HEIGHT = 127;

    // Header
    private static final int HEADER_HEIGHT = 18;
    private static final int HEADER_ITEM_SLOT_X = 0;
    private static final int HEADER_ITEM_SLOT_Y = 0;
    private static final int HEADER_TEXT_X = 26;
    private static final int HEADER_TEXT_Y = 4;

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
    private static final int PROGRESS_BAR_BORDER = 1;

    // Result slots
    private static final int RESULT_START_X = 0;
    private static final int HEADER_PADDING = 1;
    private static final int RESULT_START_Y = HEADER_HEIGHT + HEADER_PADDING;
    private static final int SLOT_SIZE = 18;
    private static int SLOTS_PER_ROW;
    private static int SLOTS_PER_PAGE;
    private static final int ITEM_RENDER_OFFSET = 0;

    // Text positions
    private static final int NO_RESULTS_TEXT_Y = 55;

    // Navigation buttons
    private static final int NAV_BUTTON_SIZE = 13;
    private static final int NAV_PREV_BUTTON_X = BACKGROUND_WIDTH - 30;
    private static final int NAV_PREV_BUTTON_Y = 3;
    private static final int NAV_NEXT_BUTTON_X = BACKGROUND_WIDTH - 15;
    private static final int NAV_NEXT_BUTTON_Y = 3;

    // Colors
    private static final int COLOR_PROGRESS_BORDER = 0xFF8B8B8B;
    private static final int COLOR_PROGRESS_BACKGROUND = 0xFF000000;
    private static final int COLOR_PROGRESS_FILL = 0xFF00AA00;
    private static final int COLOR_PROGRESS_TEXT = 0xFFFFFF;
    private static final int COLOR_HEADER_SEPARATOR = 0xFF8B8B8B;

    // Instance fields
    private final IDrawable background;
    private final IGuiHelper guiHelper;
    private final IDrawable icon;
    private final GuiIconButton searchButton;
    private final GuiIconButton nextPageButton;
    private final GuiIconButton previousPageButton;
    private static LocateStructureRecipe Recipe;
    private static int currentPage = 0;
    private static int totalPages = 0;
    private static String lastRecipeId = null;
    private static final int DEFAULT_TOOLTIP_OFFSET = 12;
    private static final int DEFAULT_TOOLTIP_BORDER_PADDING = 3 * 2;

    // Calculated values
    private final int searchButtonTextX;
    private final int searchButtonTextY;

    // Slot list to update positions
    private List<IRecipeSlotBuilder> resultSlots = new ArrayList<>();

    public LocateStructureRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.OAK_DOOR));

        // Create search button
        this.searchButton = new GuiIconButton(new DrawableBlank(0, 0), b -> LocateStructureRecipeCategory.StartSearch());
        this.searchButton.updateBounds(new ImmutableRect2i(SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT));

        // Calculate text position (centered in button)
        this.searchButtonTextX = SEARCH_BUTTON_X + SEARCH_BUTTON_WIDTH / 2;
        this.searchButtonTextY = SEARCH_BUTTON_Y + (SEARCH_BUTTON_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2;

        // Create navigation buttons
        Textures textures = Internal.getTextures();
        IDrawableStatic arrowNext = textures.getArrowNext();
        IDrawableStatic arrowPrevious = textures.getArrowPrevious();

        nextPageButton = new GuiIconButton(NAV_NEXT_BUTTON_X, NAV_NEXT_BUTTON_Y, NAV_BUTTON_SIZE, NAV_BUTTON_SIZE, arrowNext, b -> nextPage());
        previousPageButton = new GuiIconButton(NAV_PREV_BUTTON_X, NAV_PREV_BUTTON_Y, NAV_BUTTON_SIZE, NAV_BUTTON_SIZE, arrowPrevious, b -> previousPage());

        SLOTS_PER_ROW = BACKGROUND_WIDTH / SLOT_SIZE;
        SLOTS_PER_PAGE = SLOTS_PER_ROW * ((BACKGROUND_HEIGHT - RESULT_START_Y) / SLOT_SIZE);
    }

    private static void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
        }
    }

    private static void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
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
        String recipeId = recipe.itemStack.getDescriptionId();

        // Reset page if recipe changed
        if (!recipeId.equals(lastRecipeId)) {
            currentPage = 0;
            lastRecipeId = recipeId;
        }

        resultSlots.clear();
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);

        // Add header slot for the input item (always visible)
        builder.addSlot(RecipeIngredientRole.INPUT, HEADER_ITEM_SLOT_X, HEADER_ITEM_SLOT_Y)
                .addItemStack(recipe.itemStack);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, LocateStructureRecipe recipe, IFocusGroup focuses) {
        builder.addInputHandler(new ScrollnputHandler());
    }

    @Override
    public void draw(LocateStructureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        // Draw header separator line
        guiGraphics.fill(0, HEADER_HEIGHT, BACKGROUND_WIDTH, HEADER_HEIGHT + HEADER_PADDING, COLOR_HEADER_SEPARATOR);

        if (state.state == ClientSearchState.SearchState.NOT_STARTED) {
            searchButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
            guiGraphics.drawCenteredString(font, Component.translatable("structure_analyzer.jei.button.start_search").getString(), searchButtonTextX, searchButtonTextY, ChatFormatting.WHITE.getColor());
        } else if (state.state == ClientSearchState.SearchState.IN_PROGRESS) {
            drawProgressBar(guiGraphics, font, state);
        } else if (state.state == ClientSearchState.SearchState.COMPLETED) {
            if (state.foundStructures.isEmpty()) {
                String message = Component.translatable("structure_analyzer.jei.no_results").getString();
                int messageWidth = font.width(message);
                int messageX = (BACKGROUND_WIDTH - messageWidth) / 2;
                guiGraphics.drawString(font, message, messageX, NO_RESULTS_TEXT_Y,
                    ChatFormatting.RED.getColor(), true);
            } else {
                // Calculate pagination
                int resultsCount = state.foundStructures.size();
                totalPages = (int) Math.ceil(resultsCount / (double) SLOTS_PER_PAGE);
                currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

                // Draw page info in header
                String pageInfo = Component.translatable("structure_analyzer.jei.page_info", currentPage + 1, totalPages).getString();
                guiGraphics.drawString(font, pageInfo, HEADER_TEXT_X, HEADER_TEXT_Y, ChatFormatting.WHITE.getColor(), true);

                // Draw navigation buttons if multiple pages
                if (totalPages > 1) {
                    if (currentPage > 0) {
                        previousPageButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
                    }
                    if (currentPage < totalPages - 1) {
                        nextPageButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
                    }
                }

                // Draw result items for current page
                drawResultItems(guiGraphics, state, mouseX, mouseY);
            }
        }
    }

    private void drawResultItems(GuiGraphics guiGraphics, ClientSearchState.RecipeSearchState state, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        int startIndex = currentPage * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, state.foundStructures.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            int slotX = RESULT_START_X + (slotIndex % SLOTS_PER_ROW) * SLOT_SIZE;
            int slotY = RESULT_START_Y + (slotIndex / SLOTS_PER_ROW) * SLOT_SIZE;

            String structureName = state.foundStructures.get(i);
            ItemStack signStack = new ItemStack(Items.OAK_SIGN);

            // Render the item
            guiGraphics.renderItem(signStack, slotX + ITEM_RENDER_OFFSET, slotY + ITEM_RENDER_OFFSET);

            // Check if mouse is hovering for tooltip
            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                List<Component> tooltip = new ArrayList<>();
                MutableComponent nameComponent = Component.literal(structureName).withStyle(ChatFormatting.GOLD);
                tooltip.add(nameComponent);
                var totalWidth = Minecraft.getInstance().screen.width;
                var textWidth = font.width(nameComponent.getVisualOrderText());

                int slotXInScreen = (int) mouseX + (totalWidth - getWidth()) / 2;
                int relativeTooltipX = (int) mouseX;
                int overflow = (slotXInScreen + textWidth + DEFAULT_TOOLTIP_OFFSET + DEFAULT_TOOLTIP_BORDER_PADDING) - totalWidth;
                if (overflow > 0) {
                        relativeTooltipX = (int) (mouseX-overflow);
                }
                guiGraphics.renderTooltip(font, tooltip, java.util.Optional.empty(), relativeTooltipX, (int) mouseY);
            }
        }
    }

    @Override
    public boolean handleInput(LocateStructureRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        if (state.state == ClientSearchState.SearchState.NOT_STARTED) {
            if (isMouseOverButton(mouseX, mouseY, SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT)) {
                searchButton.onPress();
                return true;
            }
        } else if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty() && totalPages > 1) {
            // Handle page navigation buttons
            if (currentPage > 0 && isMouseOverButton(mouseX, mouseY, NAV_PREV_BUTTON_X, NAV_PREV_BUTTON_Y, NAV_BUTTON_SIZE, NAV_BUTTON_SIZE)) {
                previousPageButton.onPress();
                return true;
            }
            if (currentPage < totalPages - 1 && isMouseOverButton(mouseX, mouseY, NAV_NEXT_BUTTON_X, NAV_NEXT_BUTTON_Y, NAV_BUTTON_SIZE, NAV_BUTTON_SIZE)) {
                nextPageButton.onPress();
                return true;
            }
        }

        return false;
    }

    private static boolean HandleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(Recipe.itemStack.getDescriptionId());

        // Only handle scroll when there are results and multiple pages
        if (state.state == ClientSearchState.SearchState.COMPLETED && !state.foundStructures.isEmpty() && totalPages > 1) {
            if (scrollDelta > 0) {
                // Scroll up - go to previous page
                if (currentPage > 0) {
                    previousPage();
                    return true;
                }
            } else if (scrollDelta < 0) {
                // Scroll down - go to next page
                if (currentPage < totalPages - 1) {
                    nextPage();
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawProgressBar(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                 ClientSearchState.RecipeSearchState state) {
        // Draw border
        guiGraphics.fill(PROGRESS_BAR_X, PROGRESS_BAR_Y,
                        PROGRESS_BAR_X + PROGRESS_BAR_WIDTH, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT,
                        COLOR_PROGRESS_BORDER);

        // Draw background
        guiGraphics.fill(PROGRESS_BAR_X + PROGRESS_BAR_BORDER, PROGRESS_BAR_Y + PROGRESS_BAR_BORDER,
                        PROGRESS_BAR_X + PROGRESS_BAR_WIDTH - PROGRESS_BAR_BORDER,
                        PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT - PROGRESS_BAR_BORDER,
                        COLOR_PROGRESS_BACKGROUND);

        // Draw progress fill
        if (state.searchTotal > 0) {
            int innerWidth = PROGRESS_BAR_WIDTH - (PROGRESS_BAR_BORDER * 2);
            int progressWidth = (int) ((state.searchCurrent / (float) state.searchTotal) * innerWidth);
            guiGraphics.fill(PROGRESS_BAR_X + PROGRESS_BAR_BORDER, PROGRESS_BAR_Y + PROGRESS_BAR_BORDER,
                            PROGRESS_BAR_X + PROGRESS_BAR_BORDER + progressWidth,
                            PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT - PROGRESS_BAR_BORDER,
                            COLOR_PROGRESS_FILL);
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
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchState(recipe.itemStack.getDescriptionId());

        if (state.state != ClientSearchState.SearchState.NOT_STARTED) {
            return;
        }

        ClientSearchState.startSearch(recipe.itemStack.getDescriptionId());
        NetworkHandler.sendToServer(new StartSearchC2SPacket(recipe.itemStack.getDescriptionId()));
    }

    private static class ScrollnputHandler implements IJeiInputHandler {
        @Override
        public ScreenRectangle getArea() {
            return new ScreenRectangle(0, 0, LocateStructureRecipeCategory.BACKGROUND_WIDTH, LocateStructureRecipeCategory.BACKGROUND_HEIGHT);
        }

        @Override
        public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return LocateStructureRecipeCategory.HandleMouseScrolled(mouseX, mouseY, scrollDelta);
        }
    }
}
