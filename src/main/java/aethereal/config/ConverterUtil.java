package aethereal.config;

import aethereal.lib.json.JSONObject;
import aethereal.setting.BindSetting;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.setting.Setting;
import aethereal.setting.SliderSetting;
import aethereal.setting.StringSetting;

public final class ConverterUtil {
    private ConverterUtil() {
    }

    public static Object a(Setting<?> setting) {
        if (setting instanceof MultiModeSetting multiModeSetting) {
            aethereal.lib.json.JSONObject obj = new aethereal.lib.json.JSONObject();
            for (BooleanSetting child : multiModeSetting.c()) {
                obj.b(child.i(), child.c().booleanValue());
            }
            return obj;
        }
        return setting.c();
    }

    @SuppressWarnings("unchecked")
    public static void a(Setting<?> setting, Object value) {
        if (setting instanceof BooleanSetting booleanSetting && value instanceof Boolean b) {
            booleanSetting.a(b);
            return;
        }
        if (setting instanceof ModeSetting modeSetting && value instanceof String s) {
            modeSetting.a(s);
            return;
        }
        if (setting instanceof StringSetting stringSetting && value instanceof String s) {
            stringSetting.a(s);
            return;
        }
        if (setting instanceof SliderSetting sliderSetting && value instanceof Number n) {
            sliderSetting.a(Float.valueOf(n.floatValue()));
            return;
        }
        if (setting instanceof BindSetting bindSetting && value instanceof Number n) {
            bindSetting.a(Integer.valueOf(n.intValue()));
            return;
        }
        if (setting instanceof ColorSetting colorSetting && value instanceof Number n) {
            colorSetting.a(Integer.valueOf(n.intValue()));
            return;
        }
        if (setting instanceof MultiModeSetting multiModeSetting) {
            org.json.JSONObject obj = null;
            if (value instanceof org.json.JSONObject raw) {
                obj = raw;
            } else if (value instanceof aethereal.lib.json.JSONObject wrapped) {
                obj = wrapped.unwrap();
            }
            if (obj != null) {
                for (String key : obj.keySet()) {
                    BooleanSetting child = multiModeSetting.a(key);
                    if (child != null) {
                        child.a(Boolean.valueOf(obj.optBoolean(key, child.c().booleanValue())));
                    }
                }
            }
        }
    }
}
