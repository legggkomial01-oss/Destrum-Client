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
        DESTRUM_V2("Destrum-V2", "Destrum-V2"),
        DARK("Тёмный", "Dark"),
        SHADER("Шейдеры", "Shaders"),
        WALLPAPER("4K Обои", "4K Wallpaper"),
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

    public enum ShaderBackground {
        NEBULA("Туманность", "Nebula", "core/sky_nebula"),
        AURORA("Аврора", "Aurora", "core/sky_aurora"),
        STARS("Звезды", "Stars", "core/sky_stars"),
        PLASMA("Плазма", "Plasma", "core/sky_plasma"),
        NEON("Неон", "Neon", "core/sky_neon");

        private final String ru;
        private final String en;
        private final String shaderPath;

        ShaderBackground(String ru, String en, String shaderPath) {
            this.ru = ru;
            this.en = en;
            this.shaderPath = shaderPath;
        }

        public String getDisplay(Language lang) {
            return lang == Language.ENGLISH ? en : ru;
        }

        public String getShaderPath() {
            return shaderPath;
        }
    }

    public enum WallpaperBackground {
        COSMIC("Космос", "Cosmic", "pictures/bg_cosmic.png"),
        EMERALD("Изумруд", "Emerald", "pictures/bg_emerald.png"),
        SUNSET("Закат", "Sunset", "pictures/bg_sunset.png"),
        AURORA("Сияние", "Aurora", "pictures/bg_aurora.png");

        private final String ru;
        private final String en;
        private final String assetPath;

        WallpaperBackground(String ru, String en, String assetPath) {
            this.ru = ru;
            this.en = en;
            this.assetPath = assetPath;
        }

        public String getDisplay(Language lang) {
            return lang == Language.ENGLISH ? en : ru;
        }

        public Identifier getIdentifier() {
            return Identifier.of("delta", assetPath);
        }
    }

    private Language language = Language.RUSSIAN;
    private BackgroundMode backgroundMode = BackgroundMode.DEFAULT;
    private ShaderBackground shaderBackground = ShaderBackground.NEBULA;
    private WallpaperBackground wallpaperBackground = WallpaperBackground.COSMIC;

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

    public ShaderBackground getShaderBackground() {
        return shaderBackground;
    }

    public void setShaderBackground(ShaderBackground shaderBackground) {
        this.shaderBackground = shaderBackground;
        this.backgroundMode = BackgroundMode.SHADER;
        save();
    }

    public WallpaperBackground getWallpaperBackground() {
        return wallpaperBackground;
    }

    public void setWallpaperBackground(WallpaperBackground wallpaperBackground) {
        this.wallpaperBackground = wallpaperBackground;
        this.backgroundMode = BackgroundMode.WALLPAPER;
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
                try {
                    this.backgroundMode = BackgroundMode.valueOf(json.get("backgroundMode").getAsString());
                } catch (Exception ignored) {
                    this.backgroundMode = BackgroundMode.DEFAULT;
                }
            }
            if (json.has("shaderBackground")) {
                try {
                    this.shaderBackground = ShaderBackground.valueOf(json.get("shaderBackground").getAsString());
                } catch (Exception ignored) {
                    this.shaderBackground = ShaderBackground.NEBULA;
                }
            }
            if (json.has("wallpaperBackground")) {
                this.wallpaperBackground = WallpaperBackground.valueOf(json.get("wallpaperBackground").getAsString());
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
            json.addProperty("shaderBackground", this.shaderBackground.name());
            json.addProperty("wallpaperBackground", this.wallpaperBackground.name());
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
        return language == Language.ENGLISH ? "Background Mode" : "Фон главного меню";
    }

    public String getShaderSelectionLabel() {
        return language == Language.ENGLISH ? "Sky Shaders (Live)" : "Шейдеры неба (Live)";
    }

    public String getWallpaperSelectionLabel() {
        return language == Language.ENGLISH ? "4K Wallpapers" : "4K Обои";
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
