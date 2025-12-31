package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.compat.jei.ClientSearchState;
import com.saloeater.structure_analyzer.util.EMIHack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProgressUpdatedS2CPacket extends SearchRequestPacket {
    private final int current;
    private final int total;

    public ProgressUpdatedS2CPacket(SearchRequest request, int current, int total) {
        super(request);
        this.current = current;
        this.total = total;
    }

    public ProgressUpdatedS2CPacket(FriendlyByteBuf buf) {
        super(buf);
        this.current = buf.readInt();
        this.total = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        encodeRequest(buf);
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
