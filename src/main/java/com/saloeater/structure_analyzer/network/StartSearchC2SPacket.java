package com.saloeater.structure_analyzer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartSearchC2SPacket extends SearchRequestPacket {

    public StartSearchC2SPacket(SearchRequest request) {
        super(request);
    }

    public StartSearchC2SPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        encodeRequest(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerSearchManager.startSearch(ctx.get().getSender(), request);
        });
        ctx.get().setPacketHandled(true);
    }
}
