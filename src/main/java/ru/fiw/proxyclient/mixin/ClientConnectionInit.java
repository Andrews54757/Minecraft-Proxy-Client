package ru.fiw.proxyclient.mixin;

import io.netty.channel.Channel;
import net.minecraft.text.Text;
import ru.fiw.proxyclient.Proxy;
import ru.fiw.proxyclient.ProxyConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/ClientConnection$1")
public class ClientConnectionInit {
    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void connect(Channel channel, CallbackInfo cir) {
        Proxy proxy = ProxyConfig.proxy;

        if (ProxyConfig.proxyEnabled) {
            channel.pipeline().addFirst(proxy.getProxyHandler());
        }

        ProxyConfig.proxyMenuButton.setMessage(Text.literal("Proxy: " + ProxyConfig.getLastUsedProxyIp()));
    }
}