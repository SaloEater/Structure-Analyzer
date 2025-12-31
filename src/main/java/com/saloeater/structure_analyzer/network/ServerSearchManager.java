package com.saloeater.structure_analyzer.network;

import com.mojang.logging.LogUtils;
import com.saloeater.structure_analyzer.mixin.StructureTemplateAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerSearchManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, CompletableFuture<Void>> activeSearches = new HashMap<>();

    public static void startSearch(ServerPlayer player, SearchRequest request) {
        if (player == null) {
            LOGGER.warn("Received search request from null player");
            return;
        }

        UUID playerId = player.getUUID();

        if (activeSearches.containsKey(playerId)) {
            LOGGER.info("Player {} already has an active search, stopping it first", player.getName().getString());
            stopSearch(player);
        }

        LOGGER.info("Starting structure search for player {} with block: {}", player.getName().getString(), request.block());

        CompletableFuture<Void> searchTask = CompletableFuture.runAsync(() -> {
            try {
                if (request.type() == SearchRequest.TYPE_STRUCTURE) {
                    searchStructures(player, request);
                } else if (request.type() == SearchRequest.TYPE_BIOME) {
                    searchBiomes(player, request);
                }
            } catch (Exception e) {
                LOGGER.error("Error during structure search for player {}", player.getName().getString(), e);
            } finally {
                LOGGER.info("Search completed for player {}", player.getName().getString());
                activeSearches.remove(playerId);
                NetworkHandler.sendToPlayer(new SearchEndedS2CPacket(request), player);
            }
        });

        activeSearches.put(playerId, searchTask);
    }

    private static void searchBiomes(ServerPlayer player, SearchRequest request) {
        if (request.entity().isEmpty()) {
            LOGGER.warn("Received biome search request with empty entity for player {}", player.getName().getString());
            return;
        }

        var biomeEntries = ForgeRegistries.BIOMES.getEntries();
        var vanillaBiomes = player.level().getServer().registryAccess().registry(Registries.BIOME).get();

        parseBiomes(player, request, vanillaBiomes.entrySet());
        parseBiomes(player, request, biomeEntries);
    }

    private static void parseBiomes(ServerPlayer player, SearchRequest request, Set<Map.Entry<ResourceKey<Biome>, Biome>> biomeEntries) {
        for (var biomeEntry : biomeEntries) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Search interrupted for player {}", player.getName().getString());
                return;
            }
            var biome = biomeEntry.getValue();
            var key = biomeEntry.getKey();

            var mobSettings = biome.getMobSettings();
            var categories = mobSettings.getSpawnerTypes();
            for (var category : categories) {
                var spawners = mobSettings.getMobs(category);
                for (var spawner : spawners.unwrap()) {
                    var entityType = ForgeRegistries.ENTITY_TYPES.getKey(spawner.type);
                    if (entityType == null) {
                        continue;
                    }

                    if (entityType.toString().equals(request.entity())) {
                        NetworkHandler.sendToPlayer(
                                new NewStructureFoundS2CPacket(request, key.location().toString()),
                                player
                        );
                        break;
                    }
                }
            }
        }
    }

    private static void searchStructures(ServerPlayer player, SearchRequest request) {
        if (request.block().isEmpty() && request.entity().isEmpty()) {
            LOGGER.warn("Received structure search request with empty block and entity for player {}", player.getName().getString());
            return;
        }

        var manager = player.getServer().getStructureManager();
        var templates = manager.listTemplates().toList();

        int total = templates.size();
        int current = 0;

        NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(request, current, total), player);

        for (var templateId : templates) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Search interrupted for player {}", player.getName().getString());
                return;
            }

            var templateO = manager.get(templateId);
            if (templateO.isEmpty()) {
                current++;
                NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(request, current, total), player);
                continue;
            }

            var template = templateO.get();
            StructureTemplateAccessor accessor = (StructureTemplateAccessor) template;
            if (!request.block().isEmpty()) {
                var palettes = accessor.getPalettes();

                boolean found = false;
                for (var palette : palettes) {
                    var blocks = palette.blocks();
                    for (var blockInfo : blocks) {
                        var blockState = blockInfo.state();

                        if (blockState.getBlock().getDescriptionId().equals(request.block())) {
                            NetworkHandler.sendToPlayer(
                                    new NewStructureFoundS2CPacket(request, templateId.toString()),
                                    player
                            );
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            } else {
                var entities = accessor.getEntityInfoList();
                for (var entityInfo : entities) {
                    var entityType = entityInfo.nbt.getString("id");

                    if (entityType.equals(request.entity())) {
                        NetworkHandler.sendToPlayer(
                                new NewStructureFoundS2CPacket(request, templateId.toString()),
                                player
                        );
                        break;
                    }
                }
            }

            current++;

            if (current % 10 == 0 || current == total) {
                NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(request, current, total), player);
            }
        }

        if (request.entity().isEmpty()) {
            return;
        }

        var structures = player.level().getServer().registryAccess().registry(Registries.STRUCTURE).get();
        for (var structureEntry : structures.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Search interrupted for player {}", player.getName().getString());
                return;
            }
            var structure = structureEntry.getValue();
            var key = structureEntry.getKey();

            var spawnOverrides = structure.spawnOverrides().entrySet();
            for (var overrideEntry : spawnOverrides) {
                var spawners = overrideEntry.getValue();
                for (var spawner : spawners.spawns().unwrap()) {
                    var entityType = ForgeRegistries.ENTITY_TYPES.getKey(spawner.type);
                    if (entityType == null) {
                        continue;
                    }

                    if (entityType.toString().equals(request.entity())) {
                        NetworkHandler.sendToPlayer(
                                new NewStructureFoundS2CPacket(request, key.location().toString()),
                                player
                        );
                        break;
                    }
                }
            }
        }
    }

    public static void stopSearch(ServerPlayer player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUUID();
        CompletableFuture<Void> searchTask = activeSearches.get(playerId);

        if (searchTask != null) {
            LOGGER.info("Stopping search for player {}", player.getName().getString());
            searchTask.cancel(true);
            activeSearches.remove(playerId);
        }
    }

    public static void cleanupPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        stopSearch(player);
    }
}
