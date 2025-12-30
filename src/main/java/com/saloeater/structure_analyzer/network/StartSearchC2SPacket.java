package com.saloeater.structure_analyzer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartSearchC2SPacket {
    private final String blockDescriptionId;

    public StartSearchC2SPacket(String blockDescriptionId) {
        this.blockDescriptionId = blockDescriptionId;
    }

    public StartSearchC2SPacket(FriendlyByteBuf buf) {
        this.blockDescriptionId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.blockDescriptionId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerSearchManager.startSearch(ctx.get().getSender(), blockDescriptionId);
        });
        ctx.get().setPacketHandled(true);
    }

    public String getBlockDescriptionId() {
        return blockDescriptionId;
    }
}
