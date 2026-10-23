package com.terrafabrica.core.api;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.core.api.energy.IEnergyHandler;
import com.terrafabrica.core.api.heat.IHeatHandler;
import com.terrafabrica.core.api.rotation.IRotationHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * Единая точка объявления кастомных block-capabilities мода.
 *
 * <p>Контекст {@code Direction} (или {@code null} для внутреннего доступа)
 * используется для посегментного подключения энергии и вращения; тепло —
 * бесконтекстная capability ({@code Void}), т.к. теплопередача считается
 * машиной через соседние блоки.
 */
public final class TerraFabricaCapabilities {

    private TerraFabricaCapabilities() {}

    /** Энергия, Дж. Контекст — сторона блока (null = внутренний доступ). */
    public static final BlockCapability<IEnergyHandler, Direction> ENERGY =
            BlockCapability.createSided(TerraFabrica.id("energy"), IEnergyHandler.class);

    /** Тепло, K/Дж. Без контекста. */
    public static final BlockCapability<IHeatHandler, Void> HEAT =
            BlockCapability.createVoid(TerraFabrica.id("heat"), IHeatHandler.class);

    /** Вращение, RPM/Н·м. Контекст — сторона блока. */
    public static final BlockCapability<IRotationHandler, Direction> ROTATION =
            BlockCapability.createSided(TerraFabrica.id("rotation"), IRotationHandler.class);
}
