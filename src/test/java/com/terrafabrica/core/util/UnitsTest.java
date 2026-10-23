package com.terrafabrica.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверка переводов единиц СИ и формул мощности вращения.
 *
 * <p>Тесты держат два инварианта:
 * <ul>
 *   <li>конвертация Дж ↔ FE не создаёт энергию (округление вниз);</li>
 *   <li>{@code P = (2π · RPM / 60) · M} и обратные формулы взаимно согласованы.</li>
 * </ul>
 */
class UnitsTest {

    @Test
    @DisplayName("1 FE = 2.5 Дж, конвертация не создаёт энергию")
    void feJouleInvariants() {
        assertEquals(2, Units.feToJoules(1), "1 FE округляется вниз до 2 Дж");
        assertEquals(5, Units.feToJoules(2), "2 FE = 5 Дж без потерь");
        assertEquals(0, Units.feToJoules(0));
        assertEquals(2, Units.joulesToFe(5), "5 Дж = 2 FE");
        assertEquals(40, Units.joulesToFe(100), "100 Дж = 40 FE");

        // Никакого «прироста» энергии на круговом преобразовании.
        for (long j = 0; j <= 1000; j++) {
            long fe = Units.joulesToFe(j);
            assertTrue(Units.feToJoules(fe) <= j, "круговое Дж→FE→Дж не должно прибывать: " + j);
        }
    }

    @Test
    @DisplayName("Мощность вала: P = (2π·RPM/60)·M")
    void rotationPower() {
        // 60 RPM и момент 10 Н·м → ω = 2π рад/с → P ≈ 62.83 Вт.
        assertEquals(2 * Math.PI * 10, Units.rotationPower(60, 10), 1e-9);
        assertEquals(0, Units.rotationPower(60, 0), 1e-9);

        // Обратные формулы возвращают исходные значения.
        double power = 5_000;   // 5 кВт — водяное колесо
        double rpm = 90;
        double torque = Units.rotationTorque(power, rpm);
        assertEquals(power, Units.rotationPower(rpm, torque), 1e-6);
        assertEquals(rpm, Units.rotationRpm(power, torque), 1e-9);
    }

    @Test
    @DisplayName("RPM ↔ рад/с")
    void rpmRadians() {
        assertEquals(2 * Math.PI, Units.rpmToRadS(60), 1e-9);
        assertEquals(60, Units.radSToRpm(2 * Math.PI), 1e-9);
        assertEquals(0, Units.rpmToRadS(0), 0);
    }

    @Test
    @DisplayName("Температуры: 900 °C примитивной печи = 1173.15 K")
    void temperatures() {
        assertEquals(1173.15, Units.celsiusToKelvin(900), 1e-9);
        assertEquals(293.15, Units.celsiusToKelvin(20), 1e-9);
        assertEquals(900, Units.kelvinToCelsius(1173.15), 1e-9);
        assertEquals(273.15, Units.celsiusToKelvin(0), 1e-9);
    }

    @Test
    @DisplayName("Ограничение и линейная интерполяция (кривые КПД)")
    void clampAndLerp() {
        assertEquals(5, Units.clamp(10, 0, 5), 1e-9);
        assertEquals(3, Units.clamp(-1, 3, 9), 1e-9);
        // КПД паровой машины 0.5 % при малой мощности, 1 % при номинале.
        assertEquals(0.005, Units.clampLerp(0, 0, 10, 0.005, 0.01), 1e-9);
        assertEquals(0.01, Units.clampLerp(10, 0, 10, 0.005, 0.01), 1e-9);
        assertEquals(0.0075, Units.clampLerp(5, 0, 10, 0.005, 0.01), 1e-9);
        assertEquals(0.01, Units.clampLerp(99, 0, 10, 0.005, 0.01), 1e-9);
    }

    @Test
    @DisplayName("Тиков в секунду 20, в кВт·ч 3.6 МДж")
    void constants() {
        assertEquals(20, Units.TICKS_PER_SECOND);
        assertEquals(3_600_000L, Units.JOULES_PER_KWH);
        assertEquals(1_000_000L, Units.JOULES_PER_MJ);
    }
}
