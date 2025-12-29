package com.author.blank_mixin_mod.commands;

import com.author.blank_mixin_mod.mixin.StructureTemplateAccessor;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class SearchCommand {
    private static boolean inProgress = false;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("search").executes(context -> {
            if (inProgress) {
                context.getSource().sendFailure(Component.literal("Search is already in progress."));
                return 0;
            }
            inProgress = true;

            var manager = context.getSource().getLevel().getStructureManager();

            var blockType = Blocks.DIAMOND_BLOCK;

            var templates = manager.listTemplates().toList();

            for (var templateId : templates) {
                var templateO = manager.get(templateId);
                if (templateO.isEmpty()) {
                    continue;
                }

                var template = templateO.get();
                var palettes = ((StructureTemplateAccessor) template).getPalettes();
                for (var palette : palettes) {
                    var blocks = palette.blocks();
                    for (var blockInfo : blocks) {
                        var state = blockInfo.state();

                        if (state.getBlock().getDescriptionId().equals(blockType.getDescriptionId())) {
                            context.getSource().sendSuccess(() ->
                                    Component.literal("Found " + blockType.getDescriptionId() +
                                            " in template: " + templateId), false);
                        }
                    }
                }
            }
            inProgress = false;
            context.getSource().sendSuccess(() -> Component.literal("Search completed."), false);

            return 1;
        }));
    }
}
