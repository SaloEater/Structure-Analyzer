package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.util.EMIHack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProgressUpdatedS2CPacket {
    private final SearchRequest request;
    private final int current;
    private final int total;

    public ProgressUpdatedS2CPacket(SearchRequest request, int current, int total) {
        this.request = request;
        this.current = current;
        this.total = total;
    }

    public ProgressUpdatedS2CPacket(FriendlyByteBuf buf) {
        var block = buf.readUtf();
        var entity = buf.readUtf();
        this.current = buf.readInt();
        this.total = buf.readInt();
        this.request = new SearchRequest(block, entity);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.request.block());
        buf.writeUtf(this.request.entity());
        buf.writeInt(this.current);
        buf.writeInt(this.total);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSearchState.updateProgress(request, current, total);
                EMIHack.reloadEMIScreen();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
