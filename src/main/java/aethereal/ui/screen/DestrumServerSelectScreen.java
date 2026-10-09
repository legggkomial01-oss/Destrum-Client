package aethereal.ui.screen;

import aethereal.config.ThemeInfo;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.render.ColorUtil;
import aethereal.render.CrispTexture;
import aethereal.render.DestrumIconRenderer;
import aethereal.render.Draw2DProcessor;
import aethereal.render.Fonts;
import aethereal.render.ScaleUtil;
import aethereal.util.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DestrumServerSelectScreen extends Screen {
    private final Screen parent;
    private ServerList serverList;
    private final List<ServerInfo> allServers = new ArrayList<>();
    private final List<ServerInfo> filteredServers = new ArrayList<>();
    private final Map<String, CrispTexture> icons = new HashMap<>();
    private final Map<String, Float> hoverAnimations = new HashMap<>();
    private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();

    private String searchQuery = "";
    private boolean searchFocused = false;
    private float scrollX = 0.0f;
    private float targetScrollX = 0.0f;
    private boolean isDragging = false;
    private double lastDragMouseX = 0.0;

    private ServerInfo contextServer = null;
    private float contextMenuX = 0.0f;
    private float contextMenuY = 0.0f;
    private boolean serversLoaded = false;
    private volatile boolean closed = false;
    private float openProgress = 0.0f;

    public DestrumServerSelectScreen(Screen parent) {
        super(Text.literal("Сетевая игра"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.serversLoaded) {
            loadServers();
        }
    }

    private void loadServers() {
        this.serverList = new ServerList(Interface.aM_);
        this.serverList.loadFile();
        this.allServers.clear();
        for (int i = 0; i < this.serverList.size(); i++) {
            this.allServers.add(this.serverList.get(i));
        }
        this.serversLoaded = true;
        filterServers();

        // Offload all DNS resolving, pinger connections, and favicon decompression to background threads
        List<ServerInfo> serversToPing = new ArrayList<>(this.allServers);
        CompletableFuture.runAsync(() -> {
            for (ServerInfo s : serversToPing) {
                if (this.closed) break;
                loadFaviconAsync(s);
                try {
                    this.pinger.add(s, () -> loadFaviconAsync(s), () -> {});
                } catch (Exception ignored) {
                }
            }
        }, Util.getMainWorkerExecutor());
    }

    private void loadFaviconAsync(ServerInfo s) {
        byte[] favicon = s.getFavicon();
        if (favicon != null && favicon.length > 0) {
            CompletableFuture.runAsync(() -> {
                if (this.closed) return;
                try {
                    NativeImage raw = NativeImage.read(favicon);
                    NativeImage crisp = CrispTexture.upscaleToCrisp(raw);
                    raw.close();
                    if (crisp != null) {
                        RenderSystem.recordRenderCall(() -> {
                            if (this.closed) {
                                crisp.close();
                                return;
                            }
                            CrispTexture ct = this.icons.computeIfAbsent(s.address, k -> new CrispTexture("server_" + Math.abs(s.address.hashCode())));
                            ct.upload(crisp);
                        });
                    }
                } catch (Exception ignored) {
                }
            }, Util.getMainWorkerExecutor());
        }
    }

    private void filterServers() {
        this.filteredServers.clear();
        String query = this.searchQuery.trim().toLowerCase();
        for (ServerInfo s : this.allServers) {
            if (query.isEmpty() || s.name.toLowerCase().contains(query) || s.address.toLowerCase().contains(query)) {
                this.filteredServers.add(s);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.pinger.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        int width = Interface.aM_.getWindow().getScaledWidth();
        int height = Interface.aM_.getWindow().getScaledHeight();

        // 1. Dynamic background continuation
        MainScreen.a(context, width, height, (int) dA, (int) dA2, 1.0f);

        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        int sunsetAccent = ColorUtil.a(255, 175, 65, 255); // Warm sunset golden amber matching Destrum-V2

        // Smooth opening ease-out animation
        this.openProgress = MathUtil.c(this.openProgress, 1.0f, 10.0f);
        float ease = (float) (1.0 - Math.pow(1.0 - this.openProgress, 3.0));

        matrices.push();
        matrices.translate(width * 0.5f, height * 0.5f + (1.0f - ease) * 16.0f, 0.0f);
        float enterScale = 0.96f + 0.04f * ease;
        matrices.scale(enterScale, enterScale, 1.0f);
        matrices.translate(-width * 0.5f, -height * 0.5f, 0.0f);

        // Smooth scroll interpolation
        float cardW = 120.0f;
        float cardH = 120.0f;
        float gap = 20.0f;
        int totalItems = this.filteredServers.size() + 1; // +1 for "Add Server"
        float totalWidth = totalItems * cardW + (totalItems - 1) * gap;
        float maxScroll = Math.max(0.0f, totalWidth - (width - 120.0f));
        this.targetScrollX = MathHelper.clamp(this.targetScrollX, 0.0f, maxScroll);
        this.scrollX = MathUtil.c(this.scrollX, this.targetScrollX, 14.0f);

        // Dark ambient overlay
        draw.a(matrices, 0.0f, 0.0f, (float) width, (float) height, 0.0f, ColorUtil.a(0, 0, 0, (int) (45.0f * ease)));

        // 2. Top Header
        // Back button
        float backX = 22.0f;
        float backY = 18.0f;
        float backSize = 22.0f;
        boolean backHover = MathUtil.a(dA, dA2, backX, backY, backSize, backSize);
        draw.a(matrices, backX, backY, backSize, backSize, backSize * 0.5f, ColorUtil.a(20, 24, 34, backHover ? 210 : 150));
        draw.a(matrices, backX, backY, backSize, backSize, backSize * 0.5f, 1.0f, backHover ? sunsetAccent : ColorUtil.a(255, 255, 255, 40));
        Fonts.c.a(matrices, "←", backX + 7.0f, backY + 5.0f, 8.5f, backHover ? sunsetAccent : -1);

        // Direct connect button (top right)
        float directW = 86.0f;
        float directH = 18.0f;
        float directX = width - directW - 22.0f;
        float directY = 18.0f;
        boolean directHover = MathUtil.a(dA, dA2, directX, directY, directW, directH);
        draw.a(matrices, directX, directY, directW, directH, 9.0f, ColorUtil.a(20, 24, 34, directHover ? 210 : 150));
        draw.a(matrices, directX, directY, directW, directH, 9.0f, 1.0f, directHover ? sunsetAccent : ColorUtil.a(255, 255, 255, 40));
        String directLabel = "Прямой ввод";
        float dirW = Fonts.c.a(directLabel, 6.75f);
        Fonts.c.a(matrices, directLabel, directX + (directW - dirW) * 0.5f, directY + 5.0f, 6.75f, directHover ? sunsetAccent : -1);

        // Title & Subtitle
        String title = "Сетевая игра";
        float titleW = Fonts.c.a(title, 13.0f);
        Fonts.c.a(matrices, title, (width - titleW) * 0.5f, 18.0f, 13.0f, -1);

        String subtitle = "(ПКМ по серверу изменить или удалить)";
        float subW = Fonts.c.a(subtitle, 7.0f);
        Fonts.c.a(matrices, subtitle, (width - subW) * 0.5f, 34.5f, 7.0f, ColorUtil.a(180, 185, 200, 200));

        // Search Bar
        float searchW = 190.0f;
        float searchH = 19.0f;
        float searchX = (width - searchW) * 0.5f;
        float searchY = 48.0f;
        boolean searchHover = MathUtil.a(dA, dA2, searchX, searchY, searchW, searchH);
        draw.a(matrices, searchX, searchY, searchW, searchH, 9.5f, ColorUtil.a(15, 18, 26, 185));
        draw.a(matrices, searchX, searchY, searchW, searchH, 9.5f, 1.0f, this.searchFocused ? sunsetAccent : (searchHover ? ColorUtil.a(255, 255, 255, 75) : ColorUtil.a(255, 255, 255, 30)));
        Fonts.a.a(matrices, "G", searchX + 8.0f, searchY + 5.5f, 8.0f, this.searchFocused ? sunsetAccent : ColorUtil.a(180, 185, 200, 190));

        String displayText = this.searchQuery.isEmpty() && !this.searchFocused ? "Поиск..." : this.searchQuery;
        int textColor = this.searchQuery.isEmpty() && !this.searchFocused ? ColorUtil.a(140, 145, 160, 180) : -1;
        Fonts.c.a(matrices, displayText, searchX + 22.0f, searchY + 5.5f, 7.5f, textColor);
        if (this.searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            float textOffset = Fonts.c.a(this.searchQuery, 7.5f);
            draw.a(matrices, searchX + 23.0f + textOffset, searchY + 4.5f, 1.0f, 10.0f, 0.5f, sunsetAccent);
        }

        // 3. Carousel Cards
        float startX = 60.0f;
        if (totalWidth < (width - 120.0f)) {
            startX = (width - totalWidth) * 0.5f;
        }
        float cardY = (height - cardH) * 0.5f + 12.0f;

        // Render each server card
        for (int i = 0; i < this.filteredServers.size(); i++) {
            ServerInfo server = this.filteredServers.get(i);
            float curCardX = startX + i * (cardW + gap) - this.scrollX;

            if (curCardX + cardW < -20.0f || curCardX > width + 20.0f) {
                continue;
            }

            boolean cardHover = MathUtil.a(dA, dA2, curCardX, cardY, cardW, cardH) && this.contextServer == null;
            float anim = this.hoverAnimations.getOrDefault(server.address, 0.0f);
            anim = MathUtil.c(anim, cardHover ? 1.0f : 0.0f, 12.0f);
            this.hoverAnimations.put(server.address, anim);

            float drawCardY = cardY - (anim * 4.0f);
            float radius = 16.0f;

            // Background & border
            draw.a(matrices, curCardX, drawCardY, cardW, cardH, radius, ColorUtil.a(16, 20, 28, (int) (155.0f + anim * 50.0f)));
            int borderColor = ColorUtil.a(ColorUtil.a(255, 255, 255, 30), sunsetAccent, anim);
            draw.a(matrices, curCardX, drawCardY, cardW, cardH, radius, 1.0f + anim * 0.5f, borderColor);

            // Crisp favicon image (fills card cleanly with 5px padding)
            float iconPad = 5.0f;
            float iconSize = cardW - (iconPad * 2.0f);
            CrispTexture icon = this.icons.get(server.address);
            Identifier iconId = (icon != null) ? icon.getId() : Identifier.ofVanilla("textures/misc/unknown_server.png");
            draw.a(matrices, iconId, curCardX + iconPad, drawCardY + iconPad, iconSize, iconSize, 13.0f, -1);

            // Ping badge in top-right of card
            int pingCol = (server.ping < 0) ? ColorUtil.a(200, 70, 70, 230) : (server.ping < 120 ? ColorUtil.a(80, 220, 110, 230) : ColorUtil.a(230, 180, 50, 230));
            float dotSize = 5.0f;
            draw.a(matrices, curCardX + cardW - 14.0f, drawCardY + 10.0f, dotSize, dotSize, dotSize * 0.5f, pingCol);

            // Server name below card
            String name = server.name;
            if (Fonts.c.a(name, 7.5f) > cardW) {
                while (name.length() > 3 && Fonts.c.a(name + "...", 7.5f) > cardW) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "...";
            }
            float nameW = Fonts.c.a(name, 7.5f);
            Fonts.c.a(matrices, name, curCardX + (cardW - nameW) * 0.5f, drawCardY + cardH + 7.5f, 7.5f, -1);

            // Online players or IP below name
            String infoText = (server.playerCountLabel != null) ? server.playerCountLabel.getString() : server.address;
            float infoW = Fonts.c.a(infoText, 6.25f);
            Fonts.c.a(matrices, infoText, curCardX + (cardW - infoW) * 0.5f, drawCardY + cardH + 18.0f, 6.25f, ColorUtil.a(170, 175, 190, 205));
        }

        // Last card: Add Server ("+")
        int plusIndex = this.filteredServers.size();
        float plusCardX = startX + plusIndex * (cardW + gap) - this.scrollX;
        if (plusCardX + cardW >= -20.0f && plusCardX <= width + 20.0f) {
            boolean plusHover = MathUtil.a(dA, dA2, plusCardX, cardY, cardW, cardH) && this.contextServer == null;
            float plusAnim = this.hoverAnimations.getOrDefault("__plus__", 0.0f);
            plusAnim = MathUtil.c(plusAnim, plusHover ? 1.0f : 0.0f, 12.0f);
            this.hoverAnimations.put("__plus__", plusAnim);

            float drawPlusY = cardY - (plusAnim * 4.0f);
            float plusRadius = 16.0f;

            draw.a(matrices, plusCardX, drawPlusY, cardW, cardH, plusRadius, ColorUtil.a(18, 22, 32, (int) (140.0f + plusAnim * 50.0f)));
            int plusBorder = ColorUtil.a(ColorUtil.a(255, 255, 255, 30), sunsetAccent, plusAnim);
            draw.a(matrices, plusCardX, drawPlusY, cardW, cardH, plusRadius, 1.0f + plusAnim * 0.5f, plusBorder);

            // 3D Volumetric '+' Plus Badge centered
            DestrumIconRenderer.render3DPlusBadge(matrices, draw, plusCardX + cardW * 0.5f, drawPlusY + cardH * 0.44f, plusHover, plusAnim, sunsetAccent);

            // Label below card
            String plusLabel = "Добавить сервер";
            float labelW = Fonts.c.a(plusLabel, 7.5f);
            Fonts.c.a(matrices, plusLabel, plusCardX + (cardW - labelW) * 0.5f, drawPlusY + cardH + 7.5f, 7.5f, plusHover ? sunsetAccent : -1);
        }

        // 4. Context Menu Popup (ПКМ)
        if (this.contextServer != null) {
            renderContextMenu(matrices, draw, dA, dA2, sunsetAccent);
        }

        matrices.pop();
        ScaleUtil.a(context);
    }

    private void renderContextMenu(MatrixStack matrices, Draw2DProcessor draw, double mouseX, double mouseY, int primary) {
        float menuW = 115.0f;
        float itemH = 18.0f;
        float menuH = itemH * 3.0f + 8.0f;
        float mX = this.contextMenuX;
        float mY = this.contextMenuY;

        draw.a(matrices, mX, mY, menuW, menuH, 8.0f, ColorUtil.a(12, 14, 20, 245));
        draw.a(matrices, mX, mY, menuW, menuH, 8.0f, 1.0f, ColorUtil.a(255, 255, 255, 45));

        String[] items = {"▶  Подключиться", "✎  Редактировать", "🗑  Удалить"};
        for (int i = 0; i < items.length; i++) {
            float itemY = mY + 4.0f + i * itemH;
            boolean itemHover = MathUtil.a(mouseX, mouseY, mX + 2.0f, itemY, menuW - 4.0f, itemH);
            if (itemHover) {
                draw.a(matrices, mX + 4.0f, itemY, menuW - 8.0f, itemH, 5.0f, ColorUtil.a(primary, 50));
            }
            int itemCol = (i == 2) ? (itemHover ? ColorUtil.a(255, 90, 90, 255) : ColorUtil.a(240, 110, 110, 220)) : (itemHover ? primary : ColorUtil.a(200, 205, 220, 230));
            Fonts.c.a(matrices, items[i], mX + 10.0f, itemY + 5.0f, 7.0f, itemCol);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);

        // Handle context menu click
        if (this.contextServer != null) {
            float menuW = 115.0f;
            float itemH = 18.0f;
            float menuH = itemH * 3.0f + 8.0f;
            if (MathUtil.a(dA, dA2, this.contextMenuX, this.contextMenuY, menuW, menuH)) {
                int index = (int) ((dA2 - (this.contextMenuY + 4.0f)) / itemH);
                handleContextAction(index, this.contextServer);
                this.contextServer = null;
                return true;
            } else {
                this.contextServer = null;
                return true;
            }
        }

        // Back button
        float backX = 22.0f;
        float backY = 18.0f;
        float backSize = 22.0f;
        if (button == 0 && MathUtil.a(dA, dA2, backX, backY, backSize, backSize)) {
            close();
            return true;
        }

        // Direct connect button
        int width = Interface.aM_.getWindow().getScaledWidth();
        float directW = 86.0f;
        float directH = 18.0f;
        float directX = width - directW - 22.0f;
        float directY = 18.0f;
        if (button == 0 && MathUtil.a(dA, dA2, directX, directY, directW, directH)) {
            Interface.aM_.setScreen(new DirectConnectScreen(this, confirmed -> {
                if (confirmed) {
                    // connected
                }
                Interface.aM_.setScreen(this);
            }, new ServerInfo("Minecraft Server", "", ServerInfo.ServerType.OTHER)));
            return true;
        }

        // Search Bar focus
        float searchW = 190.0f;
        float searchH = 19.0f;
        float searchX = (width - searchW) * 0.5f;
        float searchY = 48.0f;
        if (MathUtil.a(dA, dA2, searchX, searchY, searchW, searchH)) {
            this.searchFocused = true;
            return true;
        } else {
            this.searchFocused = false;
        }

        // Carousel click
        float cardW = 120.0f;
        float cardH = 120.0f;
        float gap = 20.0f;
        int totalItems = this.filteredServers.size() + 1;
        float totalWidth = totalItems * cardW + (totalItems - 1) * gap;
        float startX = 60.0f;
        if (totalWidth < (width - 120.0f)) {
            startX = (width - totalWidth) * 0.5f;
        }
        int height = Interface.aM_.getWindow().getScaledHeight();
        float cardY = (height - cardH) * 0.5f + 12.0f;

        // Check server cards
        for (int i = 0; i < this.filteredServers.size(); i++) {
            float curCardX = startX + i * (cardW + gap) - this.scrollX;
            if (MathUtil.a(dA, dA2, curCardX, cardY, cardW, cardH)) {
                ServerInfo server = this.filteredServers.get(i);
                if (button == 0) {
                    connectToServer(server);
                } else if (button == 1) {
                    this.contextServer = server;
                    this.contextMenuX = (float) dA;
                    this.contextMenuY = (float) dA2;
                }
                return true;
            }
        }

        // Check "+" card
        int plusIndex = this.filteredServers.size();
        float plusCardX = startX + plusIndex * (cardW + gap) - this.scrollX;
        if (button == 0 && MathUtil.a(dA, dA2, plusCardX, cardY, cardW, cardH)) {
            ServerInfo newServer = new ServerInfo("Minecraft Server", "localhost", ServerInfo.ServerType.OTHER);
            Interface.aM_.setScreen(new AddServerScreen(this, confirmed -> {
                if (confirmed) {
                    this.serverList.add(newServer, false);
                    this.serverList.saveFile();
                    this.serversLoaded = false;
                    loadServers();
                }
                Interface.aM_.setScreen(this);
            }, newServer));
            return true;
        }

        // Start horizontal drag
        if (button == 0) {
            this.isDragging = true;
            this.lastDragMouseX = dA;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleContextAction(int actionIndex, ServerInfo server) {
        if (server == null) return;
        switch (actionIndex) {
            case 0 -> connectToServer(server);
            case 1 -> {
                Interface.aM_.setScreen(new AddServerScreen(this, confirmed -> {
                    if (confirmed) {
                        this.serverList.saveFile();
                        this.serversLoaded = false;
                        loadServers();
                    }
                    Interface.aM_.setScreen(this);
                }, server));
            }
            case 2 -> {
                this.serverList.remove(server);
                this.serverList.saveFile();
                this.serversLoaded = false;
                loadServers();
            }
        }
    }

    private void connectToServer(ServerInfo server) {
        if (server == null) return;
        ConnectScreen.connect(this, Interface.aM_, ServerAddress.parse(server.address), server, false, null);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isDragging && button == 0) {
            double dA = MathUtil.scale(mouseX, 2);
            double diff = dA - this.lastDragMouseX;
            this.targetScrollX -= (float) diff;
            this.lastDragMouseX = dA;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.targetScrollX -= (float) verticalAmount * 40.0f;
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searchFocused) {
            this.searchQuery += chr;
            filterServers();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchFocused) {
            if (keyCode == 259 && !this.searchQuery.isEmpty()) { // Backspace
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                filterServers();
                return true;
            } else if (keyCode == 256 || keyCode == 257) { // Escape or Enter
                this.searchFocused = false;
                return true;
            }
        }
        if (keyCode == 256) { // Escape
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        Interface.aM_.setScreen(this.parent);
    }

    @Override
    public void removed() {
        this.closed = true;
        this.pinger.cancel();
        for (CrispTexture icon : this.icons.values()) {
            try {
                icon.close();
            } catch (Exception ignored) {
            }
        }
        this.icons.clear();
    }
}
