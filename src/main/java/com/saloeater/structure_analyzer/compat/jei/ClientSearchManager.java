package com.saloeater.structure_analyzer.compat.jei;

import com.mojang.logging.LogUtils;
import com.saloeater.structure_analyzer.mixin.StructureTemplateAccessor;
import com.saloeater.structure_analyzer.network.SearchRequest;
import com.saloeater.structure_analyzer.util.EMIHack;
import com.saloeater.structure_analyzer.util.JEIHackStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientSearchManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CompletableFuture<Void> activeSearch = null;

    public static void startSearch(SearchRequest request) {
        if (activeSearch != null && !activeSearch.isDone()) {
            LOGGER.info("Search already in progress, stopping it first");
            stopSearch();
        }

        LOGGER.info("Starting client-side search with type: {}", request.type());

        activeSearch = CompletableFuture.runAsync(() -> {
            try {
                if (request.type() == SearchRequest.TYPE_STRUCTURE) {
                    searchStructures(request);
                } else if (request.type() == SearchRequest.TYPE_BIOME) {
                    searchBiomes(request);
                }
            } catch (Exception e) {
                LOGGER.error("Error during search", e);
            }
        }).thenRun(() -> {
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
            activeSearch.cancel(true);
            activeSearch = null;
            LOGGER.info("Search stopped");
        }
    }

    private static void searchBiomes(SearchRequest request) {
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
            if (Thread.currentThread().isInterrupted()) {
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

    private static void searchStructures(SearchRequest request) {
        if (request.block() == null && request.entity() == null) {
            LOGGER.warn("Received structure search request with null block and entity");
            return;
        }

        FileToIdConverter lister = new FileToIdConverter("structures", ".nbt");
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        var level = Minecraft.getInstance().level;
        if (level == null) {
            LOGGER.warn("Cannot search structures - level is null");
            return;
        }

        // Collect all structures from all packs
        var packs = resourceManager.listPacks().toList();
        Map<ResourceLocation, InputStream> structureMap = new HashMap<>();

        for (var pack : packs) {
            pack.listResources(PackType.SERVER_DATA, "minecraft", "structures", (resourceLocation, ioSupplier) -> {
                try {
                    structureMap.put(resourceLocation, ioSupplier.get());
                } catch (IOException e) {
                    LOGGER.error("Error reading structure from pack: {}", resourceLocation, e);
                }
            });
        }

        int total = structureMap.size();
        int current = 0;

        Minecraft.getInstance().execute(() -> {
            ClientSearchState.updateProgress(request, 0, total);
        });

        for (var entry : structureMap.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Search interrupted");
                return;
            }

            ResourceLocation structureId = entry.getKey();
            try (InputStream stream = entry.getValue()) {
                CompoundTag tag = NbtIo.readCompressed(stream);
                var template = new StructureTemplate();
                var blocks = level.registryAccess().registry(Registries.BLOCK);
                if (blocks.isEmpty()) {
                    continue;
                }
                template.load(blocks.get().asLookup(), tag);
                StructureTemplateAccessor accessor = (StructureTemplateAccessor) template;

                boolean found = false;

                // Search by block
                if (request.block() != null) {
                    var palettes = accessor.getPalettes();
                    for (var palette : palettes) {
                        for (var blockInfo : palette.blocks()) {
                            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockInfo.state().getBlock());
                            if (blockId != null && blockId.equals(request.block())) {
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                }

                // Search by entity
                if (!found && request.entity() != null) {
                    var entityInfoList = accessor.getEntityInfoList();
                    for (var entityInfo : entityInfoList) {
                        var entityNbt = entityInfo.nbt;
                        if (entityNbt.contains("id")) {
                            ResourceLocation entityId = ResourceLocation.tryParse(entityNbt.getString("id"));
                            if (entityId != null && entityId.equals(request.entity())) {
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if (found) {
                    ResourceLocation structureName = lister.fileToId(structureId);
                    LOGGER.info("Found structure: {}", structureName);
                    Minecraft.getInstance().execute(() -> {
                        ClientSearchState.addFoundStructure(request, structureName);
                    });
                }
            } catch (IOException e) {
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
}
