package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.network.SearchRequest;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class LocatedStructureIngredient extends EmiStack {
    private ResourceLocation name;
    private boolean isEvenRow;
    private int slotX;
    private int slotY;
    private int type;

    public LocatedStructureIngredient(ResourceLocation name, boolean isEvenRow, int slotX, int slotY, int type) {
        this.name = name;
        this.isEvenRow = isEvenRow;
        this.slotX = slotX;
        this.slotY = slotY;
        this.type = type;
    }

    public boolean isStructureType() {
        return type == SearchRequest.TYPE_STRUCTURE;
    }

    public boolean isBiomeType() {
        return type == SearchRequest.TYPE_BIOME;
    }

    @Override
    public EmiStack copy() {
        return new LocatedStructureIngredient(this.name, this.isEvenRow, this.slotX, this.slotY, this.type);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of();
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        ItemStack itemStack;
        if (isStructureType()) {
            // Structures: Oak/Birch doors
            if (isEvenRow) {
                itemStack = new ItemStack(Blocks.OAK_DOOR);
            } else {
                itemStack = new ItemStack(Blocks.BIRCH_DOOR);
            }
        } else {
            // Biomes: Grass/Dirt
            if (isEvenRow) {
                itemStack = new ItemStack(Blocks.GRASS_BLOCK);
            } else {
                itemStack = new ItemStack(Blocks.DIRT);
            }
        }

        draw.renderItem(itemStack, slotX + 1, slotY + 1);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        String registryName;
        if (isBiomeType()) {
            registryName = "biome";
        } else {
            registryName = "structure";
        }

        ClientTooltipComponent name = ClientTooltipComponent.create(Component.translatable(this.name.toLanguageKey(registryName)).withStyle(ChatFormatting.WHITE).getVisualOrderText());
        String namespace = this.name.getNamespace();
        String mod = EmiUtil.getModName(namespace);
        ClientTooltipComponent modId = ClientTooltipComponent.create(Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC).getVisualOrderText());
        return List.of(name, modId);
    }

    @Override
    public Component getName() {
        return Component.literal("Located Structure: " + this.name);
    }
}
