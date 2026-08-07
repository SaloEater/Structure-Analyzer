package com.saloeater.structure_analyzer.compat.jei;

import com.mojang.logging.LogUtils;
import com.saloeater.structure_analyzer.network.SearchRequest;
import com.saloeater.structure_analyzer.util.EMIHack;
import com.saloeater.structure_analyzer.util.JEIHackStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientSearchManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CompletableFuture<Void> activeSearch = null;
    // CompletableFuture.cancel() never interrupts the running thread, so the search loops
    // watch this flag instead of Thread.isInterrupted()
    private static AtomicBoolean cancelled = new AtomicBoolean(false);

    public static void startSearch(SearchRequest request) {
        if (activeSearch != null && !activeSearch.isDone()) {
            LOGGER.info("Search already in progress, stopping it first");
            stopSearch();
        }

        LOGGER.info("Starting client-side search with type: {}", request.type());

        AtomicBoolean token = new AtomicBoolean(false);
        cancelled = token;

        activeSearch = CompletableFuture.runAsync(() -> {
            try {
                if (request.type() == SearchRequest.TYPE_STRUCTURE) {
                    searchStructures(request, token);
                } else if (request.type() == SearchRequest.TYPE_BIOME) {
                    searchBiomes(request, token);
                }
            } catch (Exception e) {
                LOGGER.error("Error during search", e);
            }
        }).thenRun(() -> {
            if (token.get()) {
                return;
            }
            LOGGER.info("Search completed");
            Minecraft.getInstance().execute(() -> {
                ClientSearchState.markSearchCompleted(request);
                JEIHackStorage.shouldUpdateLayout = true;
                EMIHack.reloadEMIScreen();
            });
        });
    }

    public static void stopSearch() {
        if (activeSearch != null) {
            cancelled.set(true);
            activeSearch.cancel(true);
            activeSearch = null;
            LOGGER.info("Search stopped");
        }
    }

    private static void searchBiomes(SearchRequest request, AtomicBoolean cancelled) {
        if (request.entity() == null) {
            LOGGER.warn("Received biome search request with null entity");
            return;
        }

        var level = Minecraft.getInstance().level;
        if (level == null) {
            LOGGER.warn("Cannot search biomes - level is null");
            return;
        }

        var biomeRegistry = level.registryAccess().registry(ForgeRegistries.Keys.BIOMES);
        if (biomeRegistry.isEmpty()) {
            LOGGER.warn("Cannot search biomes - registry is empty");
            return;
        }

        var biomes = biomeRegistry.get();
        int total = biomes.size();
        int current = 0;

        Minecraft.getInstance().execute(() -> {
            ClientSearchState.updateProgress(request, 0, total);
        });

        for (var biomeEntry : biomes.entrySet()) {
            if (cancelled.get()) {
                LOGGER.info("Search interrupted");
                return;
            }

            var biome = biomeEntry.getValue();
            var mobSettings = biome.getMobSettings();

            var entityTypes = level.registryAccess().registry(ForgeRegistries.Keys.ENTITY_TYPES).get();
            var category = entityTypes.get(request.entity()).getCategory();
            // Check if this biome spawns the requested entity
            boolean found = mobSettings.getMobs(category).unwrap().stream()
                    .anyMatch(spawnerEntry -> {
                        ResourceLocation entityId = entityTypes.getKey(spawnerEntry.type);
                        return entityId != null && entityId.equals(request.entity());
                    });

            if (found) {
                ResourceLocation biomeId = biomeRegistry.get().getKey(biome);
                if (biomeId != null) {
                    LOGGER.info("Found biome: {}", biomeId);
                    Minecraft.getInstance().execute(() -> {
                        ClientSearchState.addFoundStructure(request, biomeId);
                    });
                }
            }

            current++;
            int finalCurrent = current;
            Minecraft.getInstance().execute(() -> {
                ClientSearchState.updateProgress(request, finalCurrent, total);
                EMIHack.reloadEMIScreen();
            });
        }
    }

    private static void searchStructures(SearchRequest request, AtomicBoolean cancelled) {
        if (request.block() == null && request.entity() == null) {
            LOGGER.warn("Received structure search request with null block and entity");
            return;
        }

        FileToIdConverter lister = new FileToIdConverter("structures", ".nbt");
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        // Collect all structures from all packs. Streams are opened lazily inside the loop below -
        // opening them here would keep a handle on every structure file for the whole search.
        var packs = resourceManager.listPacks().toList();
        Map<ResourceLocation, IoSupplier<InputStream>> structureMap = new HashMap<>();

        for (var pack : packs) {
            for (String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
                pack.listResources(PackType.SERVER_DATA, namespace, "structures", (resourceLocation, ioSupplier) -> {
                    if (resourceLocation.getPath().endsWith(".nbt")) {
                        structureMap.put(resourceLocation, ioSupplier);
                    }
                });
            }
        }

        int total = structureMap.size();
        int current = 0;

        Minecraft.getInstance().execute(() -> {
            ClientSearchState.updateProgress(request, 0, total);
        });

        for (var entry : structureMap.entrySet()) {
            if (cancelled.get()) {
                LOGGER.info("Search interrupted");
                return;
            }

            ResourceLocation structureId = entry.getKey();
            try (InputStream stream = entry.getValue().get()) {
                CompoundTag tag = NbtIo.readCompressed(stream);

                if (matchesStructure(tag, request)) {
                    ResourceLocation structureName = lister.fileToId(structureId);
                    LOGGER.info("Found structure: {}", structureName);
                    Minecraft.getInstance().execute(() -> {
                        ClientSearchState.addFoundStructure(request, structureName);
                    });
                }
            } catch (Exception e) {
                // Never let a single unreadable structure - or a third party mixin throwing while
                // reading one - abort the rest of the search
                LOGGER.error("Error loading structure: {}", structureId, e);
            }

            current++;
            int finalCurrent = current;
            Minecraft.getInstance().execute(() -> {
                ClientSearchState.updateProgress(request, finalCurrent, total);
                EMIHack.reloadEMIScreen();
            });
        }
    }

    // The structure NBT is read directly instead of going through StructureTemplate.load():
    // loading resolves every palette entry against the block registry, which makes blocks from
    // absent mods silently collapse into air (so they can never be matched) and lets mixins other
    // mods put on StructureBlockInfo throw while we are only trying to read ids.
    private static boolean matchesStructure(CompoundTag tag, SearchRequest request) {
        if (request.block() != null && containsBlock(tag, request.block())) {
            return true;
        }

        if (request.entity() == null) {
            return false;
        }

        // Search by entity
        ListTag entities = tag.getList("entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < entities.size(); i++) {
            if (matchesEntityId(entities.getCompound(i).getCompound("nbt"), request.entity())) {
                return true;
            }
        }

        // Search by spawners that spawn the entity - block entity nbt lives on the block entries,
        // not on the palette
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag blockNbt = blocks.getCompound(i).getCompound("nbt");
            if (!blockNbt.isEmpty() && spawnerSpawnsEntity(blockNbt, request.entity())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsBlock(CompoundTag tag, ResourceLocation block) {
        // Structures with block variants store "palettes" (a list of palettes) instead of "palette"
        ListTag palettes = tag.getList("palettes", Tag.TAG_LIST);
        if (!palettes.isEmpty()) {
            for (int i = 0; i < palettes.size(); i++) {
                if (paletteContains(palettes.getList(i), block)) {
                    return true;
                }
            }
            return false;
        }
        return paletteContains(tag.getList("palette", Tag.TAG_COMPOUND), block);
    }

    private static boolean paletteContains(ListTag palette, ResourceLocation block) {
        for (int i = 0; i < palette.size(); i++) {
            if (block.equals(ResourceLocation.tryParse(palette.getCompound(i).getString("Name")))) {
                return true;
            }
        }
        return false;
    }

    // Matches any block entity using the vanilla spawner NBT format (SpawnData/SpawnPotentials),
    // including modded spawner blocks that reuse it
    private static boolean spawnerSpawnsEntity(CompoundTag blockNbt, ResourceLocation entity) {
        if (blockNbt.contains("SpawnData", Tag.TAG_COMPOUND)
                && matchesSpawnEntry(blockNbt.getCompound("SpawnData"), entity)) {
            return true;
        }
        if (blockNbt.contains("SpawnPotentials", Tag.TAG_LIST)) {
            ListTag potentials = blockNbt.getList("SpawnPotentials", Tag.TAG_COMPOUND);
            for (int i = 0; i < potentials.size(); i++) {
                CompoundTag potential = potentials.getCompound(i);
                // 1.18+ format
                if (potential.contains("data", Tag.TAG_COMPOUND)
                        && matchesSpawnEntry(potential.getCompound("data"), entity)) {
                    return true;
                }
                // pre-1.18 format, in case a datapack ships old templates (they bypass DataFixers here)
                if (potential.contains("Entity", Tag.TAG_COMPOUND)
                        && matchesEntityId(potential.getCompound("Entity"), entity)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesSpawnEntry(CompoundTag spawnData, ResourceLocation entity) {
        // 1.18+: {entity: {id: ...}}; pre-1.18: {id: ...}
        if (spawnData.contains("entity", Tag.TAG_COMPOUND)) {
            return matchesEntityId(spawnData.getCompound("entity"), entity);
        }
        return matchesEntityId(spawnData, entity);
    }

    private static boolean matchesEntityId(CompoundTag tag, ResourceLocation entity) {
        return tag.contains("id", Tag.TAG_STRING)
                && entity.equals(ResourceLocation.tryParse(tag.getString("id")));
    }
}
