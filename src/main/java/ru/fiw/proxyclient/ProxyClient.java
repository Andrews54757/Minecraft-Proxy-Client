package ru.fiw.proxyclient;

import net.fabricmc.api.ModInitializer;

public class ProxyClient implements ModInitializer {
    @Override
    public void onInitialize() {
        ProxyConfig.loadConfig();
    }
}
