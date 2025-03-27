package ru.fiw.proxyclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import ru.fiw.proxyclient.GuiProxy;
import ru.fiw.proxyclient.ProxyConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenOpen {
    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        ProxyConfig.loadConfig();

        MultiplayerScreen ms = (MultiplayerScreen) (Object) this;
        ProxyConfig.proxyMenuButton = ButtonWidget.builder(Text.literal("Proxy: " + ProxyConfig.getLastUsedProxyIp()), (buttonWidget) -> {
            MinecraftClient.getInstance().setScreen(new GuiProxy(ms));
        }).dimensions(ms.width - 125 - 100, 5, 120, 20).build();

        ScreenAccessor si = (ScreenAccessor) ms;
        si.getDrawables().add(ProxyConfig.proxyMenuButton);
        si.getSelectables().add(ProxyConfig.proxyMenuButton);
        si.getChildren().add(ProxyConfig.proxyMenuButton);
    }
}
