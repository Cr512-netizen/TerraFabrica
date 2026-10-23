package com.terrafabrica.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Клиентская конфигурация Terra Fabrica (ModConfigSpec, тип CLIENT).
 *
 * <p>Отвечает за HUD (при наличии специальных предметов-датчиков), подробные
 * тултипы и отображение заблокированных рецептов.
 */
public final class TFClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // ------------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------------
    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_ENERGY;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_TEMPERATURE;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_ROTATION;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_ELECTRICAL;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_RADIATION;

    // ------------------------------------------------------------------
    // Тултипы
    // ------------------------------------------------------------------
    public static final ModConfigSpec.BooleanValue TOOLTIP_DETAILED;
    public static final ModConfigSpec.BooleanValue TOOLTIP_SHOW_LOCKED_RECIPES;

    static {
        BUILDER.push("hud");
        HUD_ENABLED = BUILDER.comment("Включить HUD мода (требует предмет-датчик).")
                .define("enabled", true);
        HUD_SHOW_ENERGY = BUILDER.comment("Показывать энергию (Дж/Вт).")
                .define("showEnergy", true);
        HUD_SHOW_TEMPERATURE = BUILDER.comment("Показывать температуру (K/°C).")
                .define("showTemperature", true);
        HUD_SHOW_ROTATION = BUILDER.comment("Показывать вращение (RPM/Н·м/Вт).")
                .define("showRotation", true);
        HUD_SHOW_ELECTRICAL = BUILDER.comment("Показывать напряжение и ток (В/А).")
                .define("showElectrical", true);
        HUD_SHOW_RADIATION = BUILDER.comment("Показывать радиационный фон.")
                .define("showRadiation", true);
        BUILDER.pop();

        BUILDER.push("tooltips");
        TOOLTIP_DETAILED = BUILDER.comment("Подробные тултипы с параметрами машин и инструментов.")
                .define("detailed", true);
        TOOLTIP_SHOW_LOCKED_RECIPES = BUILDER.comment("Показывать в тултипах предметов название требуемого исследования.")
                .define("showLockedRecipes", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private TFClientConfig() {}
}
