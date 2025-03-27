package ru.fiw.proxyclient;

import java.net.Proxy;
import java.net.SocketAddress;

import ru.fiw.proxyclient.Proxy.ProxyType;

import java.net.InetSocketAddress;

public class ProxyObject extends Proxy {
    public ProxyObject() {
        super(Proxy.Type.HTTP, new InetSocketAddress(0));
    }
    
    @Override
    public Type type() {
        if (!ru.fiw.proxyclient.ProxyConfig.proxyEnabled) {
            return Proxy.Type.DIRECT;
        }

        ProxyType type = ru.fiw.proxyclient.ProxyConfig.proxy.getType();
        switch (type) {
            case SOCKS4:
                return Proxy.Type.SOCKS;
            case SOCKS5:
                return Proxy.Type.SOCKS;
            case HTTP:
                return Proxy.Type.HTTP;
            default:
                return Proxy.Type.SOCKS;
        }
    }

    @Override
    public SocketAddress address() {
        return ru.fiw.proxyclient.ProxyConfig.proxy.getInetSocketAddress();
    }
}