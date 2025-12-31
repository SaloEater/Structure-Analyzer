package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.util.EMIHack;
import com.saloeater.structure_analyzer.util.JEIHackStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SearchEndedS2CPacket {
    private final SearchRequest request;

    public SearchEndedS2CPacket(SearchRequest blockDescriptionId) {
        this.request = blockDescriptionId;
    }

    public SearchEndedS2CPacket(FriendlyByteBuf buf) {
        var block = buf.readUtf();
        var entity = buf.readUtf();
        this.request = new SearchRequest(block, entity);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.request.block());
        buf.writeUtf(this.request.entity());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSearchState.markSearchCompleted(request);
                JEIHackStorage.shouldUpdateLayout = true;
                EMIHack.reloadEMIScreen();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
