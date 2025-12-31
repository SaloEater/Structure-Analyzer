package com.saloeater.structure_analyzer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartSearchC2SPacket {
    private final SearchRequest request;

    public StartSearchC2SPacket(SearchRequest request) {
        this.request = request;
    }

    public StartSearchC2SPacket(FriendlyByteBuf buf) {
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
            ServerSearchManager.startSearch(ctx.get().getSender(), request);
        });
        ctx.get().setPacketHandled(true);
    }
}
