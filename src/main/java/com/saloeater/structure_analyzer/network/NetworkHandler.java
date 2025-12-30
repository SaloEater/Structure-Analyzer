package com.saloeater.structure_analyzer.network;

import com.saloeater.structure_analyzer.StructureAnalyzer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StructureAnalyzer.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client to Server packets
        INSTANCE.messageBuilder(StartSearchC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StartSearchC2SPacket::new)
                .encoder(StartSearchC2SPacket::encode)
                .consumerMainThread(StartSearchC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(StopSearchC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StopSearchC2SPacket::new)
                .encoder(StopSearchC2SPacket::encode)
                .consumerMainThread(StopSearchC2SPacket::handle)
                .add();

        // Server to Client packets
        INSTANCE.messageBuilder(NewStructureFoundS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NewStructureFoundS2CPacket::new)
                .encoder(NewStructureFoundS2CPacket::encode)
                .consumerMainThread(NewStructureFoundS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(SearchEndedS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SearchEndedS2CPacket::new)
                .encoder(SearchEndedS2CPacket::encode)
                .consumerMainThread(SearchEndedS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(ProgressUpdatedS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ProgressUpdatedS2CPacket::new)
                .encoder(ProgressUpdatedS2CPacket::encode)
                .consumerMainThread(ProgressUpdatedS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
