package com.loademon.afkfarm.fake;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;

/**
 * A connection with no client behind it. Everything the server sends to the fake player is dropped,
 * so nothing queues up waiting for a channel that never opens.
 */
final class FakeConnection extends Connection {

    FakeConnection() {
        super(PacketFlow.SERVERBOUND);
    }

    @Override
    public void send(final Packet<?> packet) {
    }

    @Override
    public void send(final Packet<?> packet, final @Nullable ChannelFutureListener listener) {
    }

    @Override
    public void send(final Packet<?> packet, final @Nullable ChannelFutureListener listener, final boolean flush) {
    }

    @Override
    public void sendPacketAndScheduleNettyTask(final Packet<?> packet, final boolean flush, final Runnable after) {
    }

    @Override
    public void flushChannel() {
    }
}
