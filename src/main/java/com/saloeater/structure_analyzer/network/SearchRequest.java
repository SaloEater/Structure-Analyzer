package com.saloeater.structure_analyzer.network;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record SearchRequest(@Nullable ResourceLocation block, @Nullable ResourceLocation entity, int type) {
    public static int TYPE_STRUCTURE = 0;
    public static int TYPE_BIOME = 1;
}
