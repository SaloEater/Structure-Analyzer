package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NewStructureFoundS2CPacket extends SearchRequestPacket {
    private final String structureName;

    public NewStructureFoundS2CPacket(SearchRequest request, String structureName) {
        super(request);
        this.structureName = structureName;
    }

    public NewStructureFoundS2CPacket(FriendlyByteBuf buf) {
        super(buf);
        this.structureName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        encodeRequest(buf);
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
