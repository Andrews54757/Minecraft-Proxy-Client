package ru.fiw.proxyclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.fiw.proxyclient.Proxy.ProxyType;

import org.apache.commons.lang3.StringUtils;

public class GuiProxy extends Screen {
    private ProxyType proxyType;

    private TextFieldWidget ipPort;
    private TextFieldWidget username;
    private TextFieldWidget password;
    private CheckboxWidget enabledCheck;

    private Screen parentScreen;

    private String msg = "";

    private int[] positionY;
    private int positionX;

    private TestPing testPing = new TestPing();


    private static String text_proxy = Text.translatable("ui.proxyclient.options.proxy").getString();


    public GuiProxy(Screen parentScreen) {
        super(Text.literal(text_proxy));
        this.parentScreen = parentScreen;
    }

    private static boolean isValidIpPort(String ipP) {
        String[] split = ipP.split(":");
        if (split.length > 1) {
            if (!StringUtils.isNumeric(split[1])) return false;
            int port = Integer.parseInt(split[1]);
            if (port < 0 || port > 0xFFFF) return false;
            return true;
        } else {
            return false;
        }
    }

    private boolean checkProxy() {
        if (!isValidIpPort(ipPort.getText())) {
            msg = Formatting.RED + Text.translatable("ui.proxyclient.options.invalidIpPort").getString();
            this.ipPort.setFocused(true);
            return false;
        }
        return true;
    }

    private void centerButtons(int amount, int buttonLength, int gap) {
        positionX = (this.width / 2) - (buttonLength / 2);
        positionY = new int[amount];
        int center = (this.height + amount * gap) / 2;
        int buttonStarts = center - (amount * gap);
        for (int i = 0; i != amount; i++) {
            positionY[i] = buttonStarts + (gap * i);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        msg = "";
        testPing.state = "";
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        super.render(context, mouseX, mouseY, partialTicks);

        if (enabledCheck.isChecked() && !isValidIpPort(ipPort.getText())) {
            enabledCheck.onPress();
        }

        context.drawTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.options.proxyType").getString(), this.width / 2 - 150, positionY[1] + 5, 10526880);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.options.auth").getString(), this.width / 2, positionY[3] + 8, Formatting.WHITE.getColorValue());
        context.drawTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.options.ipPort").getString(), this.width / 2 - 150, positionY[2] + 5, 10526880);

        this.ipPort.render(context, mouseX, mouseY, partialTicks);
        if (this.proxyType == ProxyType.SOCKS4) {
            context.drawTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.auth.id").getString(), this.width / 2 - 150, positionY[4] + 5, 10526880);
            this.username.render(context, mouseX, mouseY, partialTicks);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.auth.password").getString(), this.width / 2 - 150, positionY[5] + 5, 10526880);
            context.drawTextWithShadow(this.textRenderer, Text.translatable("ui.proxyclient.auth.username").getString(), this.width / 2 - 150, positionY[4] + 5, 10526880);
            this.username.render(context, mouseX, mouseY, partialTicks);
            this.password.render(context, mouseX, mouseY, partialTicks);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, !msg.isEmpty() ? msg : testPing.state, this.width / 2, positionY[6] + 5, 10526880);
    }

    @Override
    public void tick() {
        testPing.pingPendingNetworks();
    }

    @Override
    public void init() {
//        MinecraftClient.getInstance().keyboard.setRepeatEvents(true);
        int buttonLength = 160;
        centerButtons(10, buttonLength, 26);

        this.proxyType = ProxyConfig.proxy.getType();

        ButtonWidget proxyType = ButtonWidget.builder(Text.literal(Proxy.getProxyStringFromType(this.proxyType)), button -> {
            this.proxyType = Proxy.getNextProxyType(this.proxyType);
            button.setMessage(Text.literal(Proxy.getProxyStringFromType(this.proxyType)));
        }).dimensions(positionX, positionY[1], buttonLength, 20).build();
        this.addDrawableChild(proxyType);

        this.ipPort = new TextFieldWidget(this.textRenderer, positionX, positionY[2], buttonLength, 20, Text.literal(""));
        this.ipPort.setText(ProxyConfig.proxy.host + ":" + ProxyConfig.proxy.port);
        this.ipPort.setMaxLength(1024);
        this.ipPort.setFocused(true);
        this.addSelectableChild(this.ipPort);

        this.username = new TextFieldWidget(this.textRenderer, positionX, positionY[4], buttonLength, 20, Text.literal(""));
        this.username.setMaxLength(255);
        this.username.setText(ProxyConfig.proxy.username);
        this.addSelectableChild(this.username);

        this.password = new TextFieldWidget(this.textRenderer, positionX, positionY[5], buttonLength, 20, Text.literal(""));
        this.password.setMaxLength(255);
        this.password.setText(ProxyConfig.proxy.password);
        this.addSelectableChild(this.password);

        int posXButtons = (this.width / 2) - (((buttonLength / 2) * 3) / 2);

        ButtonWidget apply = ButtonWidget.builder(Text.translatable("ui.proxyclient.options.apply"), button -> {
            if (checkProxy()) {
                String ipPort = this.ipPort.getText();
                String host = Proxy.getHostFromIpPort(ipPort);
                int port = Proxy.getPortFromIpPort(ipPort);

                ProxyConfig.proxy = new Proxy(this.proxyType, host, port, username.getText(), password.getText());
                ProxyConfig.proxyEnabled = enabledCheck.isChecked();
                ProxyConfig.saveConfig();
                ProxyConfig.loadConfig();
                MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
            }
        }).dimensions(posXButtons, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addDrawableChild(apply);

        ButtonWidget test = ButtonWidget.builder(Text.translatable("ui.proxyclient.options.test"), (button) -> {
            if (ipPort.getText().isEmpty() || ipPort.getText().equalsIgnoreCase("none")) {
                msg = Formatting.RED + Text.translatable("ui.proxyclient.err.specProxy").getString();
                return;
            }
            if (checkProxy()) {
                testPing = new TestPing();
                String ipPort = this.ipPort.getText();
                String host = Proxy.getHostFromIpPort(ipPort);
                int port = Proxy.getPortFromIpPort(ipPort);
                testPing.run("mc.hypixel.net", 25565, new Proxy(this.proxyType, host, port, username.getText(), password.getText()));
            }
        }).dimensions(posXButtons + buttonLength / 2 + 3, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addDrawableChild(test);

        CheckboxWidget.Builder checkboxBuilder = CheckboxWidget.builder(Text.translatable("ui.proxyclient.options.proxyEnabled"), this.textRenderer);
        checkboxBuilder.pos((this.width / 2) - (15 + textRenderer.getWidth(Text.translatable("ui.proxyclient.options.proxyEnabled"))) / 2, positionY[7]);
        if (ProxyConfig.proxyEnabled) {
            checkboxBuilder.checked(ProxyConfig.proxyEnabled);
        }
        this.enabledCheck = checkboxBuilder.build();
        this.addDrawableChild(this.enabledCheck);

        ButtonWidget cancel = ButtonWidget.builder(Text.translatable("ui.proxyclient.options.cancel"), (button) -> {
            MinecraftClient.getInstance().setScreen(parentScreen);
        }).dimensions(posXButtons + (buttonLength / 2 + 3) * 2, positionY[8], buttonLength / 2 - 3, 20).build();
        this.addDrawableChild(cancel);
    }

    @Override
    public void close() {
        msg = "";
//        MinecraftClient.getInstance().keyboard.setRepeatEvents(false);
    }
}