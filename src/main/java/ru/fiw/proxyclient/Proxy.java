package ru.fiw.proxyclient;

import java.net.InetSocketAddress;

import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;

public class Proxy {
    public String host = "";
    public int port = 0;
    public ProxyType type = ProxyType.SOCKS5;
    public String username = "";
    public String password = "";

    public Proxy() {
    }

    public Proxy(ProxyType type, String host, int port, String username, String password) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public ProxyType getType() {
        return type;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(this.host, this.port);
    }

    public ProxyHandler getProxyHandler() {
        InetSocketAddress address = getInetSocketAddress();
        switch (type) {
            case SOCKS4:
                return new Socks4ProxyHandler(address);
            case SOCKS5:
                if (username.isEmpty() || password.isEmpty()) {
                    return new Socks5ProxyHandler(address);
                }
                return new Socks5ProxyHandler(address, username, password);
            case HTTP:
                if (username.isEmpty() || password.isEmpty()) {
                    return new HttpProxyHandler(address);
                }
                return new HttpProxyHandler(address, username, password);
            default:
                if (username.isEmpty() || password.isEmpty()) {
                    return new Socks5ProxyHandler(address);
                }
                return new Socks5ProxyHandler(address, username, password);
        }
    }
    
    public static String getHostFromIpPort(String ipPort) {
        return ipPort.split(":")[0];
    }

    public static int getPortFromIpPort(String ipPort) {
        String[] split = ipPort.split(":");
        if (split.length > 1) {
            return Integer.parseInt(split[1]);
        } else {
            return 0;
        }
    }

    public static ProxyType getProxyTypeFromString(String proxy) {
        switch (proxy.toLowerCase()) {
            case "socks4":
                return ProxyType.SOCKS4;
            case "socks5":
                return ProxyType.SOCKS5;
            case "http":
                return ProxyType.HTTP;
            default:
                return ProxyType.SOCKS5;
        }
    }

    public static String getProxyStringFromType(ProxyType type) {
        switch (type) {
            case SOCKS4:
                return "SOCKS4";
            case SOCKS5:
                return "SOCKS5";
            case HTTP:
                return "HTTP";
            default:
                return "SOCKS5";
        }
    }

    public static ProxyType getNextProxyType(ProxyType type) {
        switch (type) {
            case SOCKS4:
                return ProxyType.SOCKS5;
            case SOCKS5:
                return ProxyType.HTTP;
            case HTTP:
                return ProxyType.SOCKS4;
            default:
                return ProxyType.SOCKS5;
        }
    }

    public enum ProxyType {
        SOCKS4,
        SOCKS5,
        HTTP
    }
}
