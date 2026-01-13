package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.mixin.SlotWidgetAccessor;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class LocateStructureRecipe implements EmiRecipe {
    // Textures
    private static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("textures/gui/slider.png");
    private static final ResourceLocation SEARCH_BUTTON_TEXTURE = new ResourceLocation(StructureAnalyzer.MODID,"textures/gui/slider_short.png");
    private static final int SLIDER_BACKGROUND_UV_Y = 0;
    private static final int SLIDER_FILL_UV_Y = 40;

    // Background dimensions
    private static final int BACKGROUND_WIDTH = 180;
    private static final int BACKGROUND_HEIGHT = 127;

    // Header
    private static final int HEADER_HEIGHT = 18;
    private static final int HEADER_ITEM_SLOT_X = 0;
    private static final int HEADER_ITEM_SLOT_Y = 0;
    private static final int HEADER_RESULTS_Y = 5;
    private static final int SLOT_SIZE = 18;

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
    private static final int RESULT_GRID_Y = HEADER_HEIGHT + HEADER_PADDING + 5;
    private static final int RESULT_GRID_COLUMNS = 9;
    private static final int RESULT_GRID_ROWS = 5;

    // Text positions
    private static final int NO_RESULTS_TEXT_Y = 55;

    // Colors
    private static final int COLOR_PROGRESS_TEXT = 0xFFFFFF;
    private static final int COLOR_HEADER_SEPARATOR = 0xFF8B8B8B;

    // Scroll bar
    private static final ResourceLocation SCROLL_BACKGROUND_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");
    private static final ResourceLocation SCROLL_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");
    private static final int SCROLL_BAR_WIDTH = 14;
    private static final int SCROLL_BAR_X = BACKGROUND_WIDTH - SCROLL_BAR_WIDTH - 2;
    private static final int SCROLL_HANDLE_HEIGHT = 15;
    private static final int SCROLL_BORDER_SIZE = 1;

    private static SlotWidget targetStackSlot = new SlotWidget(null, HEADER_ITEM_SLOT_X, HEADER_ITEM_SLOT_Y);

    // Scroll state (only one recipe visible at a time)
    private static int scrollPos = 0;
    private static boolean isScrolling = false;

    private static int type;

    protected void setSlotItemStack(EmiStack stack) {
        ((SlotWidgetAccessor) targetStackSlot).SetStack(stack);
    }

    protected void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.LOCATE_STRUCTURE_CATEGORY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return BACKGROUND_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return BACKGROUND_HEIGHT;
    }

    public abstract ClientSearchState.RecipeSearchState getSearchState();

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var state = getSearchState();

        LocateStructureCategory category = (LocateStructureCategory) getCategory();

        // Handle mouse click - check if clicking on scroll bar
        double clickMouseX = category.getClickMouseX();
        double clickMouseY = category.getClickMouseY();
        if (clickMouseX != 0 || clickMouseY != 0) {
            handleMouseClicked(clickMouseX, clickMouseY, state);
        }

        // Handle mouse release - stop scrolling
        if (!category.isMouseClicked()) {
            isScrolling = false;
        }

        // Handle mouse scroll
        double scrollDelta = category.getScrollDelta();
        if (scrollDelta != 0) {
            handleMouseScroll(scrollDelta);
        }

        // Handle mouse drag
        double dragDeltaX = category.getDragDeltaX();
        double dragDeltaY = category.getDragDeltaY();
        double dragMouseX = category.getDragMouseX();
        double dragMouseY = category.getDragMouseY();
        if (dragDeltaX != 0 || dragDeltaY != 0) {
            handleMouseDragged(dragMouseX, dragMouseY, dragDeltaX, dragDeltaY, state);
        }

        widgets.addTexture(SLIDER_TEXTURE, 0, HEADER_ITEM_SLOT_Y, BACKGROUND_WIDTH, HEADER_HEIGHT, 3, 3, 14, 14, 256, 526);

        // Add header slot for input item
        widgets.add(targetStackSlot);

        if (state.state == ClientSearchState.SearchState.NOT_STARTED) {
            // Draw search button
            int searchButtonTextX = SEARCH_BUTTON_X + SEARCH_BUTTON_WIDTH / 2;
            int searchButtonTextY = SEARCH_BUTTON_Y + (SEARCH_BUTTON_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2 + 1;

            widgets.addButton(SEARCH_BUTTON_X, SEARCH_BUTTON_Y, SEARCH_BUTTON_WIDTH, SEARCH_BUTTON_HEIGHT, 0, 40,
                SEARCH_BUTTON_TEXTURE,
                () -> true,
                (mouseX, mouseY, button) -> {
                    startSearch();
                });

            MutableComponent startSearchText = Component.translatable("structure_analyzer.jei.button.start_search");
            var textWidth = Minecraft.getInstance().font.width(startSearchText.getString());
            widgets.addText(startSearchText, searchButtonTextX - textWidth / 2, searchButtonTextY, ChatFormatting.WHITE.getColor(), true);

        } else if (state.state == ClientSearchState.SearchState.IN_PROGRESS) {
            // Draw progress bar
            drawProgressBar(widgets, state);

        } else if (state.state == ClientSearchState.SearchState.COMPLETED) {
            if (state.foundStructures.isEmpty()) {
                // Draw "no results" message
                String message = Component.translatable("structure_analyzer.jei.no_results").getString();
                var font = Minecraft.getInstance().font;
                int messageWidth = font.width(message);
                int messageX = (BACKGROUND_WIDTH - messageWidth) / 2;

                widgets.addText(Component.literal(message), messageX, NO_RESULTS_TEXT_Y,
                    ChatFormatting.DARK_RED.getColor(), true);
            } else {
                // Draw results found text in 3 colors
                drawResultsFoundText(widgets, state);

                // Calculate scroll parameters
                int totalResults = state.foundStructures.size();
                int visibleRows = RESULT_GRID_ROWS;
                int totalRows = (int) Math.ceil(totalResults / (double) RESULT_GRID_COLUMNS);
                int scrollableRows = Math.max(0, totalRows - visibleRows);

                // Clamp scroll position
                scrollPos = Math.max(0, Math.min(scrollPos, scrollableRows));

                // Draw scroll bar if needed
                if (scrollableRows > 0) {
                    int scrollBarHeight = RESULT_GRID_ROWS * SLOT_SIZE;
                    int scrollBarY = RESULT_GRID_Y;

                    // Draw scroll bar background
                    widgets.addTexture(SCROLL_BACKGROUND_TEXTURE, SCROLL_BAR_X, scrollBarY, SCROLL_BAR_WIDTH, scrollBarHeight,
                        174, 17, 14, 112, 256, 256);

                    // Calculate scroll handle position
                    double scrollPercentage = scrollableRows == 0 ? 0 : (double) scrollPos / scrollableRows;
                    int scrollHandleYRange = scrollBarHeight - SCROLL_HANDLE_HEIGHT - SCROLL_BORDER_SIZE * 2;
                    int scrollHandleY = scrollBarY + SCROLL_BORDER_SIZE + (int) (scrollPercentage * scrollHandleYRange);

                    // Draw scroll handle
                    widgets.addTexture(SCROLL_TEXTURE, SCROLL_BAR_X + SCROLL_BORDER_SIZE, scrollHandleY,
                        12, SCROLL_HANDLE_HEIGHT, 232, 0, 12, 15, 256, 256);
                }

                // Add result slots for visible rows
                int startRow = scrollPos;
                int endRow = Math.min(startRow + visibleRows, totalRows);

                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < RESULT_GRID_COLUMNS; col++) {
                        int index = row * RESULT_GRID_COLUMNS + col;
                        if (index >= totalResults) break;

                        var structureName = state.foundStructures.get(index);
                        int displayRow = row - startRow;
                        int slotX = col * SLOT_SIZE;
                        int slotY = RESULT_GRID_Y + displayRow * SLOT_SIZE;
                        widgets.addSlot(new LocatedStructureIngredient(structureName, row % 2 == 0, slotX, slotY), slotX, slotY);
                    }
                }
            }
        }
    }

    public abstract EmiStack getTargetStack();

    private void drawProgressBar(WidgetHolder widgets, ClientSearchState.RecipeSearchState state) {
        // Draw background texture
        widgets.addTexture(SLIDER_TEXTURE, PROGRESS_BAR_X, PROGRESS_BAR_Y,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
            0, SLIDER_BACKGROUND_UV_Y);

        // Draw progress fill
        if (state.searchTotal > 0) {
            int progressWidth = (int) ((state.searchCurrent / (float) state.searchTotal) * PROGRESS_BAR_WIDTH);
            if (progressWidth > 0) {
                widgets.addTexture(SLIDER_TEXTURE, PROGRESS_BAR_X, PROGRESS_BAR_Y,
                    progressWidth, PROGRESS_BAR_HEIGHT,
                    0, SLIDER_FILL_UV_Y);
            }
        }

        // Draw progress text
        var font = Minecraft.getInstance().font;
        String progressText = Component.translatable("structure_analyzer.jei.progress",
            state.searchCurrent, state.searchTotal).getString();
        int textWidth = font.width(progressText);
        int textX = PROGRESS_BAR_X + (PROGRESS_BAR_WIDTH - textWidth) / 2;
        int textY = PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - font.lineHeight) / 2;

        widgets.addText(Component.literal(progressText), textX, textY, COLOR_PROGRESS_TEXT, true);
    }

    private void drawResultsFoundText(WidgetHolder widgets, ClientSearchState.RecipeSearchState state) {
        var font = Minecraft.getInstance().font;

        String prefix = Component.translatable("structure_analyzer.jei.results_found.prefix").getString();
        String count = String.valueOf(state.foundStructures.size());
        String suffix = Component.translatable("structure_analyzer.jei.results_found.suffix").getString();

        int prefixWidth = font.width(prefix);
        int countWidth = font.width(count);
        int suffixWidth = font.width(suffix);
        int spaceWidth = font.width(" ");
        int totalWidth = prefixWidth + spaceWidth + countWidth + spaceWidth + suffixWidth;

        int startX = (BACKGROUND_WIDTH - totalWidth) / 2;

        // Draw "Found" in white
        widgets.addText(Component.literal(prefix), startX, HEADER_RESULTS_Y,
            ChatFormatting.WHITE.getColor(), true);

        // Draw count in gold
        widgets.addText(Component.literal(count), startX + prefixWidth + spaceWidth, HEADER_RESULTS_Y,
            ChatFormatting.GOLD.getColor(), true);

        // Draw "structures" in white
        widgets.addText(Component.literal(suffix), startX + prefixWidth + spaceWidth + countWidth + spaceWidth,
            HEADER_RESULTS_Y, ChatFormatting.WHITE.getColor(), true);
    }

    public abstract void startSearch();

    private void handleMouseClicked(double mouseX, double mouseY, ClientSearchState.RecipeSearchState state) {
        // Only handle clicks when there are results to scroll
        if (state.state != ClientSearchState.SearchState.COMPLETED || state.foundStructures.isEmpty()) {
            return;
        }

        // Calculate scroll bar dimensions
        int scrollBarHeight = RESULT_GRID_ROWS * SLOT_SIZE;
        int scrollBarY = RESULT_GRID_Y;

        // Convert screen coordinates to local coordinates
        var realX = mouseX;
        var realY = mouseY;
        var localX = realX - (Minecraft.getInstance().screen.width - BACKGROUND_WIDTH) / 2;
        var localY = realY - (Minecraft.getInstance().screen.height - BACKGROUND_HEIGHT) / 2;

        // Check if click is within scroll bar bounds
        if (localX >= SCROLL_BAR_X && localX <= SCROLL_BAR_X + SCROLL_BAR_WIDTH
                && localY >= scrollBarY && localY <= scrollBarY + scrollBarHeight) {
            isScrolling = true;
        }
    }

    private void handleMouseScroll(double delta) {
        if (delta > 0) {
            // Scroll up
            scrollPos = Math.max(0, scrollPos - 1);
        } else if (delta < 0) {
            // Scroll down
            scrollPos = scrollPos + 1;
            // Note: scrollPos will be clamped in addWidgets when calculating scrollableRows
        }
    }

    private void handleMouseDragged(double mouseX, double mouseY, double dragDeltaX, double dragDeltaY, ClientSearchState.RecipeSearchState state) {
        // Only handle drag if we're currently scrolling
        if (!isScrolling) {
            return;
        }

        // Calculate scroll bar dimensions
        int scrollBarHeight = RESULT_GRID_ROWS * SLOT_SIZE;
        int scrollBarY = RESULT_GRID_Y;

        // Calculate scroll parameters
        int totalResults = state.foundStructures.size();
        int visibleRows = RESULT_GRID_ROWS;
        int totalRows = (int) Math.ceil(totalResults / (double) RESULT_GRID_COLUMNS);
        int scrollableRows = Math.max(0, totalRows - visibleRows);

        if (scrollableRows == 0) {
            return; // Nothing to scroll
        }

        // Convert screen coordinates to local coordinates
        var realY = mouseY + dragDeltaY;
        var localY = realY - (Minecraft.getInstance().screen.height - BACKGROUND_HEIGHT) / 2;

        // Calculate relative Y position within scroll bar (using mouseY + dragDeltaY like PlayerDamageLogScreen)
        double relativeY = localY - scrollBarY - SCROLL_BORDER_SIZE;
        double percentage = relativeY / (double) (scrollBarHeight - SCROLL_BORDER_SIZE * 2);

        // Convert percentage to scroll position
        int nextPos = (int) (scrollableRows * percentage);

        // Clamp to valid range
        scrollPos = Math.max(0, Math.min(nextPos, scrollableRows));
    }
}
