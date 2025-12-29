package com.author.blank_mixin_mod.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSearchState {
    public enum SearchState {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    public static class RecipeSearchState {
        public SearchState state = SearchState.NOT_STARTED;
        public int searchTotal = 0;
        public int searchCurrent = 0;
        public List<String> foundStructures = new ArrayList<>();
    }

    private static final Map<String, RecipeSearchState> searchStateMap = new HashMap<>();

    public static RecipeSearchState getSearchState(String blockDescriptionId) {
        return searchStateMap.computeIfAbsent(blockDescriptionId, k -> new RecipeSearchState());
    }

    public static void startSearch(String blockDescriptionId) {
        RecipeSearchState state = getSearchState(blockDescriptionId);
        state.state = SearchState.IN_PROGRESS;
        state.searchCurrent = 0;
        state.searchTotal = 0;
        state.foundStructures.clear();
    }

    public static void updateProgress(String blockDescriptionId, int current, int total) {
        RecipeSearchState state = getSearchState(blockDescriptionId);
        state.searchCurrent = current;
        state.searchTotal = total;
    }

    public static void addFoundStructure(String blockDescriptionId, String structureName) {
        RecipeSearchState state = getSearchState(blockDescriptionId);
        if (!state.foundStructures.contains(structureName)) {
            state.foundStructures.add(structureName);
        }
    }

    public static void markSearchCompleted(String blockDescriptionId) {
        RecipeSearchState state = getSearchState(blockDescriptionId);
        state.state = SearchState.COMPLETED;
    }

    public static void resetSearch(String blockDescriptionId) {
        searchStateMap.remove(blockDescriptionId);
    }
}
