package com.saloeater.structure_analyzer.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract class SearchRequestPacket {
    protected final SearchRequest request;

    protected SearchRequestPacket(SearchRequest request) {
        this.request = request;
    }

    protected SearchRequestPacket(FriendlyByteBuf buf) {
        var block = buf.readUtf();
        var entity = buf.readUtf();
        var type = buf.readInt();
        this.request = new SearchRequest(block, entity, type);
    }

    protected void encodeRequest(FriendlyByteBuf buf) {
        buf.writeUtf(this.request.block());
        buf.writeUtf(this.request.entity());
        buf.writeInt(this.request.type());
    }

    public SearchRequest getRequest() {
        return request;
    }
}
