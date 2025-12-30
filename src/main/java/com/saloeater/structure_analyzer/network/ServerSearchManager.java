package com.saloeater.structure_analyzer.network;

import com.mojang.logging.LogUtils;
import com.saloeater.structure_analyzer.mixin.StructureTemplateAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerSearchManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, CompletableFuture<Void>> activeSearches = new HashMap<>();

    public static void startSearch(ServerPlayer player, String blockDescriptionId) {
        if (player == null) {
            LOGGER.warn("Received search request from null player");
            return;
        }

        UUID playerId = player.getUUID();

        if (activeSearches.containsKey(playerId)) {
            LOGGER.info("Player {} already has an active search, stopping it first", player.getName().getString());
            stopSearch(player);
        }

        LOGGER.info("Starting structure search for player {} with block: {}", player.getName().getString(), blockDescriptionId);

        CompletableFuture<Void> searchTask = CompletableFuture.runAsync(() -> {
            try {
                var manager = player.getServer().getStructureManager();
                var templates = manager.listTemplates().toList();

                int total = templates.size();
                int current = 0;

                NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(blockDescriptionId, current, total), player);

                for (var templateId : templates) {
                    if (Thread.currentThread().isInterrupted()) {
                        LOGGER.info("Search interrupted for player {}", player.getName().getString());
                        return;
                    }

                    var templateO = manager.get(templateId);
                    if (templateO.isEmpty()) {
                        current++;
                        NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(blockDescriptionId, current, total), player);
                        continue;
                    }

                    var template = templateO.get();
                    var palettes = ((StructureTemplateAccessor) template).getPalettes();

                    boolean found = false;
                    for (var palette : palettes) {
                        var blocks = palette.blocks();
                        for (var blockInfo : blocks) {
                            var blockState = blockInfo.state();

                            if (blockState.getBlock().getDescriptionId().equals(blockDescriptionId)) {
                                NetworkHandler.sendToPlayer(
                                        new NewStructureFoundS2CPacket(blockDescriptionId, templateId.toString()),
                                        player
                                );
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }

                    current++;

                    if (current % 10 == 0 || current == total) {
                        NetworkHandler.sendToPlayer(new ProgressUpdatedS2CPacket(blockDescriptionId, current, total), player);
                    }
                }

                NetworkHandler.sendToPlayer(new SearchEndedS2CPacket(blockDescriptionId), player);
                LOGGER.info("Search completed for player {}", player.getName().getString());

            } catch (Exception e) {
                LOGGER.error("Error during structure search for player {}", player.getName().getString(), e);
                NetworkHandler.sendToPlayer(new SearchEndedS2CPacket(blockDescriptionId), player);
            } finally {
                activeSearches.remove(playerId);
            }
        });

        activeSearches.put(playerId, searchTask);
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
