package net.fabricmc.example;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerMenuMod extends Screen {

    private static final String NGROK_AUTH_TOKEN = "ТВОЙ_ТОКЕН_СЮДА";

    private int maxPlayers = 5; 
    private ButtonWidget playerLimitButton;
    private ButtonWidget startServerButton;
    private boolean isStarting = false;
    private Process ngrokProcess = null;

    public ServerMenuMod() {
        super(Text.literal("Управление сервером Poplar Mod"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        playerLimitButton = ButtonWidget.builder(Text.literal("👥 Слоты: " + maxPlayers), button -> {
            if (maxPlayers == 5) maxPlayers = 10;
            else if (maxPlayers == 10) maxPlayers = 15;
            else if (maxPlayers == 15) maxPlayers = 50;
            else maxPlayers = 5;
            button.setMessage(Text.literal("👥 Слоты: " + maxPlayers));
        })
        .dimensions(centerX - 100, centerY - 30, 200, 20)
        .build();
        
        startServerButton = ButtonWidget.builder(Text.literal("🚀 ЗАПУСТИТЬ ВСЁ В 1 КЛИК"), button -> {
            if (this.client != null && this.client.server != null && !this.client.server.isRemote()) {
                startEverything();
            }
        })
        .dimensions(centerX - 100, centerY - 5, 200, 20)
        .build();

        ButtonWidget backButton = ButtonWidget.builder(Text.literal("Назад"), button -> this.close())
        .dimensions(centerX - 100, centerY + 30, 200, 20)
        .build();

        if (isStarting) {
            startServerButton.active = false;
            playerLimitButton.active = false;
        }

        this.addDrawableChild(playerLimitButton);
        this.addDrawableChild(startServerButton);
        this.addDrawableChild(backButton);
    }

    private void startEverything() {
        this.isStarting = true;
        this.startServerButton.active = false;
        this.playerLimitButton.active = false;
        this.startServerButton.setMessage(Text.literal("⏳ Магия запуска..."));

        int port = 25565;
        
        this.client.execute(() -> {
            this.client.server.openToLan(this.client.server.getDefaultGameMode(), false, port);
            this.client.server.getPlayerManager().setMaxPlayerCount(maxPlayers);
        });

        new Thread(() -> {
            try {
                sendChatMessage("§e[Poplar] Авто-настройка туннеля...");

                String os = System.getProperty("os.name").toLowerCase();
                boolean isAndroid = System.getProperty("java.vendor").contains("The Android Project") || os.contains("android");

                if (!isAndroid) {
                    Process authProc = Runtime.getRuntime().exec("ngrok config add-authtoken " + NGROK_AUTH_TOKEN);
                    authProc.waitFor();
                    ngrokProcess = Runtime.getRuntime().exec("ngrok tcp " + port);
                } else {
                    sendChatMessage("§7[Pojav] На смартфонах запусти в Termux одну команду:");
                    sendChatMessage("§bngrok tcp " + port);
                }

                Thread.sleep(3000);

                URL url = new URL("http://127.0.0.1:4040/api/tunnels");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) result.append(line);
                rd.close();

                String json = result.toString();
                if (json.contains("public_url") && json.contains("tcp://")) {
                    int start = json.indexOf("tcp://") + 6;
                    int end = json.indexOf("\"", start);
                    String publicAddress = json.substring(start, end);

                    sendChatMessage("§a✔ Сервер открыт на " + maxPlayers + " мест!");
                    sendChatMessage("§6🔗 Ссылка для друзей: §b§l" + publicAddress);
                    
                    this.client.execute(() -> this.startServerButton.setMessage(Text.literal("✅ Всё работает!")));
                } else {
                    throw new Exception("Ngrok API не вернул публичный адрес");
                }

            } catch (Exception e) {
                sendChatMessage("§c❌ Не удалось получить IP автоматически.");
                sendChatMessage("§7Включи ngrok вручную, затем нажми кнопку повторно.");
                this.client.execute(() -> {
                    this.isStarting = false;
                    this.startServerButton.active = true;
                    this.playerLimitButton.active = true;
                    this.startServerButton.setMessage(Text.literal("🔄 Повторить запуск"));
                });
            }
        }).start();
    }

    private void sendChatMessage(String text) {
        if (this.client != null && this.client.player != null) {
            this.client.execute(() -> this.client.player.sendMessage(Text.literal(text), false));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
