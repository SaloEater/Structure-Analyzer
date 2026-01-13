package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import com.saloeater.structure_analyzer.compat.jei.ClientSearchManager;
import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.network.SearchRequest;
import com.saloeater.structure_analyzer.util.EMIHack;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LocateStructureByBlockRecipe extends LocateStructureRecipe {
    private ResourceLocation id;
    private ItemStack itemStack;

    LocateStructureByBlockRecipe(Map.Entry<ResourceKey<Block>, Block> blockEntry) {
        this.itemStack = new ItemStack(blockEntry.getValue().asItem());
        this.id = new ResourceLocation(StructureAnalyzer.MODID, "/locate_structure" + blockEntry.getKey().location().getNamespace() + "_" + blockEntry.getKey().location().getPath());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        this.setSlotItemStack(getTargetStack());
        this.setType(SearchRequest.TYPE_STRUCTURE);
        super.addWidgets(widgets);
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(itemStack));
    }

    @Override
    public ClientSearchState.RecipeSearchState getSearchState() {
        var block = net.minecraft.world.level.block.Block.byItem(itemStack.getItem());
        ResourceLocation blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        return ClientSearchState.getSearchStateByStructureBlock(blockId);
    }

    @Override
    public EmiStack getTargetStack() {
        return EmiStack.of(itemStack);
    }

    @Override
    public void startSearch() {
        var block = net.minecraft.world.level.block.Block.byItem(itemStack.getItem());
        ResourceLocation blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByStructureBlock(blockId);

        if (state.state != ClientSearchState.SearchState.NOT_STARTED) {
            return;
        }

        SearchRequest request = new SearchRequest(blockId, null, SearchRequest.TYPE_STRUCTURE);
        ClientSearchState.startSearch(request);
        ClientSearchManager.startSearch(request);
        EMIHack.reloadEMIScreen();
    }

    @Override
    public int getSearchType() {
        return SearchRequest.TYPE_STRUCTURE;
    }
}
