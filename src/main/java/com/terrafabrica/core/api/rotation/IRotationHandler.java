package com.terrafabrica.core.api.rotation;

import com.terrafabrica.core.util.Units;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Собственная capability вращения Terra Fabrica.
 *
 * <p>Вращение описывается парой «обороты — момент»: мощность
 * {@code P = (2π · RPM / 60) · Torque} (Вт = рад/с · Н·м). КПД и потери на
 * передачах считает владелец (машина), а не хранилище.
 */
public interface IRotationHandler {

    /** Обороты вала, RPM (об/мин). */
    double getRPM();

    /** Крутящий момент на валу, Н·м. */
    double getTorque();

    /** Механическая мощность, Вт: {@code (2π · RPM / 60) · Torque}. */
    default double getPower() {
        return Units.rotationPower(getRPM(), getTorque());
    }

    /**
     * Может ли вращение передаваться через указанную сторону.
     * {@code null} означает «внутренний запрос» (например, от самой машины).
     */
    boolean canConnect(@Nullable Direction side);

    /**
     * Принимает вращение с указанной стороны (серверная логика сети валов).
     * Реализация обычно фиксирует входные обороты/момент до следующего тика.
     *
     * @param rpm    обороты источника
     * @param torque момент источника, Н·м
     * @return {@code true}, если вход принят
     */
    default boolean receiveRotation(double rpm, double torque) {
        return false;
    }
}
