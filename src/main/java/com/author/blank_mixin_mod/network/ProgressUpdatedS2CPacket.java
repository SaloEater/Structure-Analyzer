package com.author.blank_mixin_mod.network;

import com.author.blank_mixin_mod.compat.jei.ClientSearchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProgressUpdatedS2CPacket {
    private final String blockDescriptionId;
    private final int current;
    private final int total;

    public ProgressUpdatedS2CPacket(String blockDescriptionId, int current, int total) {
        this.blockDescriptionId = blockDescriptionId;
        this.current = current;
        this.total = total;
    }

    public ProgressUpdatedS2CPacket(FriendlyByteBuf buf) {
        this.blockDescriptionId = buf.readUtf();
        this.current = buf.readInt();
        this.total = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.blockDescriptionId);
        buf.writeInt(this.current);
        buf.writeInt(this.total);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSearchState.updateProgress(blockDescriptionId, current, total);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
