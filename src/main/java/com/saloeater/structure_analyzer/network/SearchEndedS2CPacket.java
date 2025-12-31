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
    private final String blockDescriptionId;

    public SearchEndedS2CPacket(String blockDescriptionId) {
        this.blockDescriptionId = blockDescriptionId;
    }

    public SearchEndedS2CPacket(FriendlyByteBuf buf) {
        this.blockDescriptionId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.blockDescriptionId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSearchState.markSearchCompleted(blockDescriptionId);
                JEIHackStorage.shouldUpdateLayout = true;
                EMIHack.reloadEMIScreen();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
