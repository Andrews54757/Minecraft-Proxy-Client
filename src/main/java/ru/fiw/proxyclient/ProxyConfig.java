package ru.fiw.proxyclient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ProxyConfig {
    private static final String CONFIG_PATH = MinecraftClient.getInstance().runDirectory + "/config/ProxyClientConfig.json";
    
    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static ButtonWidget proxyMenuButton;

    public static String getLastUsedProxyIp() {
        return proxy.port == 0 ? "None" : (proxy.host + ":" + proxy.port);
    }

    public static void loadConfig() {
        File configFile = new File(CONFIG_PATH);

        try {
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    System.out.println("Error creating ProxyClientConfig.json file");
                }
                return;
            }

            String configString = FileUtils.readFileToString(configFile, "UTF-8");

            if (!configString.isEmpty()) {
                JsonObject configJson = JsonParser.parseString(configString).getAsJsonObject();
                ProxyConfig.proxyEnabled = configJson.get("proxy-enabled").getAsBoolean();

                Type type = new TypeToken<Proxy>() {
                }.getType();
                
                proxy = new Gson().fromJson(configJson.get("proxy"), type);
                if (proxy == null) {
                    proxy = new Proxy();
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading ProxyClientConfig.json file");
            e.printStackTrace();
        }

        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
        
        if (proxyEnabled && proxy.port != 0) {
            if (proxy.getType() == Proxy.ProxyType.HTTP) {
                System.setProperty("http.proxyHost", proxy.host);
                System.setProperty("http.proxyPort", String.valueOf(proxy.port));
                System.setProperty("https.proxyHost", proxy.host);
                System.setProperty("https.proxyPort", String.valueOf(proxy.port));
            } else {
                System.setProperty("socksProxyHost", proxy.host);
                System.setProperty("socksProxyPort", String.valueOf(proxy.port));
            }
        }
    }

    public static void saveConfig() {
        try {
            JsonElement proxyJsonObject = new Gson().toJsonTree(proxy);
            JsonObject configJson = new JsonObject();
            configJson.addProperty("proxy-enabled", ProxyConfig.proxyEnabled);
            configJson.add("proxy", proxyJsonObject);

            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            FileUtils.write(new File(CONFIG_PATH), gsonPretty.toJson(configJson), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error writing ProxyClientConfig.json file");
            e.printStackTrace();
        }
    }
}
