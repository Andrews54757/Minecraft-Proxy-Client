package ru.fiw.proxyclient.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.client.MinecraftClient;
import ru.fiw.proxyclient.ProxyConfig;
import ru.fiw.proxyclient.ProxyObject;

@Mixin(MinecraftClient.class)
public class YggdrasilInjection {
    @Final
    @Mutable
    @Shadow
    private YggdrasilAuthenticationService authenticationService;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initInject(CallbackInfo info) {
        ProxyConfig.loadConfig();
        
        if (!ProxyConfig.proxy.username.isEmpty() && !ProxyConfig.proxy.password.isEmpty()) {
            java.net.Authenticator.setDefault(new java.net.Authenticator() {
                protected java.net.PasswordAuthentication getPasswordAuthentication() {
                    return new java.net.PasswordAuthentication(ProxyConfig.proxy.username,
                            ProxyConfig.proxy.password.toCharArray());
                }
            });
        }

        this.authenticationService = new YggdrasilAuthenticationService(new ProxyObject());
    }
}