package com.saloeater.structure_analyzer.network;

public record SearchRequest(String block, String entity, int type) {
    public static int TYPE_STRUCTURE = 0;
    public static int TYPE_BIOME = 1;
}
