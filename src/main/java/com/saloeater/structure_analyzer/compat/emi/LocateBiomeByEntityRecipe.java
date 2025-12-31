package com.saloeater.structure_analyzer.compat.emi;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.network.NetworkHandler;
import com.saloeater.structure_analyzer.network.SearchRequest;
import com.saloeater.structure_analyzer.network.StartSearchC2SPacket;
import com.saloeater.structure_analyzer.util.EMIHack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LocateBiomeByEntityRecipe extends LocateStructureRecipe {
    private final String entityId;
    private ResourceLocation id;
    private EmiStack spawnEggStack;

    LocateBiomeByEntityRecipe(Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entityTypeEntry) {
        this.spawnEggStack = getSpawnEggStack(entityTypeEntry.getValue());
        this.id = new ResourceLocation(StructureAnalyzer.MODID, "/locate_biome" + entityTypeEntry.getKey().location().getNamespace() + "_" + entityTypeEntry.getKey().location().getPath());
        String entityIdHolder = "";
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityTypeEntry.getValue());
        if (key != null) {
            entityIdHolder = key.toString();
        }
        this.entityId = entityIdHolder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        this.setSlotItemStack(getTargetStack());
        this.setType(SearchRequest.TYPE_BIOME);
        super.addWidgets(widgets);
    }

    private EmiStack getSpawnEggStack(EntityType<?> type) {
        // Try to find the spawn egg for this entity type
        Item spawnEgg = SpawnEggItem.byId(type);

        if (spawnEgg == null) {
            spawnEgg = ForgeSpawnEggItem.fromEntityType(type);
        }

        // If spawn egg doesn't exist, fall back to a default item
        if (spawnEgg == null || spawnEgg == Items.AIR) {
            spawnEgg = Items.BARRIER;
        }

        return EmiStack.of(new ItemStack(spawnEgg));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.LOCATE_BIOME_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(spawnEggStack);
    }

    @Override
    public ClientSearchState.RecipeSearchState getSearchState() {
        return ClientSearchState.getSearchStateByBiomeEntity(entityId);
    }

    @Override
    public EmiStack getTargetStack() {
        return spawnEggStack;
    }

    @Override
    public void startSearch() {
        ClientSearchState.RecipeSearchState state = ClientSearchState.getSearchStateByBiomeEntity(entityId);

        if (state.state != ClientSearchState.SearchState.NOT_STARTED) {
            return;
        }

        SearchRequest request = new SearchRequest("", entityId, SearchRequest.TYPE_BIOME);
        ClientSearchState.startSearch(request);
        NetworkHandler.sendToServer(new StartSearchC2SPacket(request));
        EMIHack.reloadEMIScreen();
    }
}
