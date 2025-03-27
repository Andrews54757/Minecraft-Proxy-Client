package ru.fiw.proxyclient;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TestPing {
    public String state = "";

    private long pingSentAt;
    private ClientConnection pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = Text.translatable("ui.proxyclient.ping.pinging", ip).getString();
        ClientConnection clientConnection;
        try {
            clientConnection = createTestClientConnection(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            state = Formatting.RED + Text.translatable("ui.proxyclient.err.cantConnect").getString();
            return;
        } catch (Exception e) {
            state = Formatting.RED + Text.translatable("ui.proxyclient.err.cantPing", ip).getString();
            return;
        }
        pingDestination = clientConnection;
        ClientQueryPacketListener clientQueryPacketListener = new ClientQueryPacketListener() {
            private boolean successful;

            @Override
            public void onPingResult(PingResultS2CPacket packet) {
                successful = true;
                pingDestination = null;
                long pingToServer = Util.getMeasuringTimeMs() - pingSentAt;
                state = Text.translatable("ui.proxyclient.ping.showPing", pingToServer).getString();
                clientConnection.disconnect(Text.translatable("multiplayer.status.finished"));
            }

            public void onResponse(QueryResponseS2CPacket packet) {
                pingSentAt = Util.getMeasuringTimeMs();
                clientConnection.send(new QueryPingC2SPacket(pingSentAt));
            }

            public void onDisconnected(DisconnectionInfo info) {
                pingDestination = null;
                if (!this.successful) {
                    state = Formatting.RED + Text.translatable("ui.proxyclient.err.cantPingReason", ip, info.reason().getString()).getString();
                }
            }

            @Override
            public boolean isConnectionOpen() {
                return true;
            }

            public ClientConnection getConnection() {
                return clientConnection;
            }
        };

        try {
            clientConnection.connect(ip, port, clientQueryPacketListener);
			clientConnection.send(QueryRequestC2SPacket.INSTANCE);
        } catch (Throwable throwable) {
            state = Formatting.RED + Text.translatable("ui.proxyclient.err.cantPing", ip).getString();
        }
    }

    private ClientConnection createTestClientConnection(InetAddress address, int port) {
        final ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);

        (new Bootstrap()).group(ClientConnection.CLIENT_IO_GROUP.get()).handler(new ChannelInitializer<>() {
            protected void initChannel(Channel channel) {
                //ClientConnection.setHandlers(channel);

                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelexception) {
                }

                ChannelPipeline channelpipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                ClientConnection.addHandlers(channelpipeline, NetworkSide.CLIENTBOUND,false, null);
                channel.pipeline().addFirst(proxy.getProxyHandler());
            }
        }).channel(NioSocketChannel.class).connect(address, port).syncUninterruptibly();
        return clientConnection;
    }

    public void pingPendingNetworks() {
        if (pingDestination != null) {
            if (pingDestination.isOpen()) {
                pingDestination.tick();
            } else {
                pingDestination.handleDisconnection();
            }
        }
    }
}
