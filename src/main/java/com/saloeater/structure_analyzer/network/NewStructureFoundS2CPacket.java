package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NewStructureFoundS2CPacket {
    private final SearchRequest request;
    private final String structureName;

    public NewStructureFoundS2CPacket(SearchRequest request, String structureName) {
        this.request = request;
        this.structureName = structureName;
    }

    public NewStructureFoundS2CPacket(FriendlyByteBuf buf) {
        var block = buf.readUtf();
        var entity = buf.readUtf();
        this.structureName = buf.readUtf();
        this.request = new SearchRequest(block, entity);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.request.block());
        buf.writeUtf(this.request.entity());
        buf.writeUtf(this.structureName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSearchState.addFoundStructure(request, structureName);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
