package com.author.blank_mixin_mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StopSearchC2SPacket {

    public StopSearchC2SPacket() {
    }

    public StopSearchC2SPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerSearchManager.stopSearch(ctx.get().getSender());
        });
        ctx.get().setPacketHandled(true);
    }
}
