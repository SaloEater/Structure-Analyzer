package com.saloeater.structure_analyzer.compat.jei;

import com.saloeater.structure_analyzer.network.SearchRequest;

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

    private static final Map<SearchRequest, RecipeSearchState> searchStateMap = new HashMap<>();

    public static RecipeSearchState getSearchState(SearchRequest request) {
        return searchStateMap.computeIfAbsent(request, k -> new RecipeSearchState());
    }

    public static RecipeSearchState getSearchStateByStructureBlock(String block) {
        return getSearchState(new SearchRequest(block, "", SearchRequest.TYPE_STRUCTURE));
    }

    public static RecipeSearchState getSearchStateByStructureEntity(String entity) {
        return getSearchState(new SearchRequest("", entity, SearchRequest.TYPE_STRUCTURE));
    }

    public static RecipeSearchState getSearchStateByBiomeEntity(String entity) {
        return getSearchState(new SearchRequest("", entity, SearchRequest.TYPE_BIOME));
    }

    public static void startSearch(SearchRequest request) {
        RecipeSearchState state = getSearchState(request);
        state.state = SearchState.IN_PROGRESS;
        state.searchCurrent = 0;
        state.searchTotal = 0;
        state.foundStructures.clear();
    }

    public static void updateProgress(SearchRequest request, int current, int total) {
        RecipeSearchState state = getSearchState(request);
        state.searchCurrent = current;
        state.searchTotal = total;
    }

    public static void addFoundStructure(SearchRequest request, String structureName) {
        RecipeSearchState state = getSearchState(request);
        if (!state.foundStructures.contains(structureName)) {
            state.foundStructures.add(structureName);
        }
    }

    public static void markSearchCompleted(SearchRequest block) {
        RecipeSearchState state = getSearchState(block);
        state.state = SearchState.COMPLETED;
    }
}
