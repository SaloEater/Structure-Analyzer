package com.saloeater.structure_analyzer.compat.emi;

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

    public LocatedStructureIngredient(ResourceLocation name, boolean isEvenRow, int slotX, int slotY) {
        this.name = name;
        this.isEvenRow = isEvenRow;
        this.slotX = slotX;
        this.slotY = slotY;
    }

    @Override
    public EmiStack copy() {
        return new LocatedStructureIngredient(this.name, this.isEvenRow, this.slotX, this.slotY);
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
        var item = isEvenRow ? Blocks.OAK_DOOR : Blocks.BIRCH_DOOR;
        draw.renderItem(new ItemStack(item.asItem()), slotX, slotY);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return List.of(ClientTooltipComponent.create(Component.literal(this.name.toLanguageKey()).withStyle(ChatFormatting.WHITE).getVisualOrderText()));
    }

    @Override
    public Component getName() {
        return Component.literal("Located Structure: " + this.name);
    }
}
