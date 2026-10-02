package aethereal.config;

import aethereal.core.Interface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class MainMenuConfig {
    private static MainMenuConfig instance;

    public enum Language {
        RUSSIAN("Русский"),
        ENGLISH("English");

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BackgroundMode {
        DEFAULT("Стандартный", "Default"),
        DARK("Тёмный", "Dark"),
        CUSTOM("Свой фон", "Custom");

        private final String ru;
        private final String en;

        BackgroundMode(String ru, String en) {
            this.ru = ru;
            this.en = en;
        }

        public String getDisplay(Language lang) {
            return lang == Language.ENGLISH ? en : ru;
        }
    }

    private Language language = Language.RUSSIAN;
    private BackgroundMode backgroundMode = BackgroundMode.DEFAULT;
    private String customImagePath = "";
    private Identifier customTextureIdentifier = null;
    private NativeImageBackedTexture customTexture = null;

    private static final Identifier CUSTOM_TEXTURE_ID = Identifier.of("delta", "custom_main_menu_bg");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized MainMenuConfig getInstance() {
        if (instance == null) {
            instance = new MainMenuConfig();
            instance.load();
        }
        return instance;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        save();
    }

    public BackgroundMode getBackgroundMode() {
        return backgroundMode;
    }

    public void setBackgroundMode(BackgroundMode backgroundMode) {
        this.backgroundMode = backgroundMode;
        save();
    }

    public String getCustomImagePath() {
        return customImagePath;
    }

    public Identifier getCustomTextureIdentifier() {
        return customTextureIdentifier;
    }

    public boolean loadCustomImage(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        try (InputStream in = new FileInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            if (customTexture != null) {
                try {
                    customTexture.close();
                } catch (Exception ignored) {
                }
            }
            customTexture = new NativeImageBackedTexture(image);
            Interface.aM_.getTextureManager().registerTexture(CUSTOM_TEXTURE_ID, customTexture);
            this.customTextureIdentifier = CUSTOM_TEXTURE_ID;
            this.customImagePath = file.getAbsolutePath();
            this.backgroundMode = BackgroundMode.CUSTOM;
            save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void resetBackground() {
        this.backgroundMode = BackgroundMode.DEFAULT;
        this.customImagePath = "";
        save();
    }

    private File getConfigFile() {
        File dir = new File(Interface.aM_.runDirectory, "configs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "destrum_menu.json");
    }

    public void load() {
        File file = getConfigFile();
        if (!file.exists()) {
            return;
        }
        try {
            String jsonStr = Files.readString(file.toPath());
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (json.has("language")) {
                this.language = Language.valueOf(json.get("language").getAsString());
            }
            if (json.has("backgroundMode")) {
                this.backgroundMode = BackgroundMode.valueOf(json.get("backgroundMode").getAsString());
            }
            if (json.has("customImagePath")) {
                this.customImagePath = json.get("customImagePath").getAsString();
                if (this.backgroundMode == BackgroundMode.CUSTOM && !this.customImagePath.isEmpty()) {
                    File imgFile = new File(this.customImagePath);
                    if (imgFile.exists()) {
                        loadCustomImage(imgFile);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("language", this.language.name());
            json.addProperty("backgroundMode", this.backgroundMode.name());
            json.addProperty("customImagePath", this.customImagePath);
            File file = getConfigFile();
            Files.writeString(file.toPath(), GSON.toJson(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Localization strings
    public String getSingleplayerText() {
        return language == Language.ENGLISH ? "Singleplayer" : "Одиночный Режим";
    }

    public String getMultiplayerText() {
        return language == Language.ENGLISH ? "Multiplayer" : "Сетевая Игра";
    }

    public String getAltManagerText() {
        return language == Language.ENGLISH ? "Alt Manager" : "Выбор аккаунта";
    }

    public String getSettingsText() {
        return language == Language.ENGLISH ? "Settings" : "Настройки";
    }

    public String getExitText() {
        return language == Language.ENGLISH ? "Quit Game" : "Выйти из игры";
    }

    public String getMenuSettingsTitle() {
        return language == Language.ENGLISH ? "Menu Settings" : "Настройки главного меню";
    }

    public String getLanguageLabel() {
        return language == Language.ENGLISH ? "Tab Language" : "Язык текста вкладок";
    }

    public String getBackgroundLabel() {
        return language == Language.ENGLISH ? "Main Menu Background" : "Фон главного меню";
    }

    public String getSelectFileText() {
        return language == Language.ENGLISH ? "Choose Image..." : "Выбрать картинку...";
    }

    public String getResetText() {
        return language == Language.ENGLISH ? "Reset" : "Сбросить";
    }

    public String getNoImageText() {
        return language == Language.ENGLISH ? "No image chosen (.png, .jpg)" : "Картинка не выбрана (.png, .jpg)";
    }
}
