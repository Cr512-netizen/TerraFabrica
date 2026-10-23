package com.terrafabrica.core.api.energy;

import com.terrafabrica.core.util.Units;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Буфер энергии в джоулях: пределы приёма/отдачи заданы в Дж/тик,
 * {@code simulate} не меняет состояние, ёмкость не переполняется.
 */
class EnergyStorageTest {

    @Test
    @DisplayName("Приём ограничен maxInput и свободной ёмкостью")
    void receiveClamps() {
        EnergyStorage storage = new EnergyStorage(10_000, 500, 400);
        assertEquals(500, storage.receiveEnergy(5_000, false), "maxInput ограничивает приём за тик");
        assertEquals(500, storage.getStoredEnergy());

        // Заполняем почти до края и проверяем, что лишнее не принимается.
        storage.setEnergy(9_800);
        assertEquals(200, storage.receiveEnergy(500, false), "свободной ёмкости только 200 Дж");
        assertEquals(10_000, storage.getStoredEnergy());
        assertEquals(0, storage.receiveEnergy(100, false), "буфер полон");
    }

    @Test
    @DisplayName("simulate не меняет запас")
    void simulateDoesNotMutate() {
        EnergyStorage storage = new EnergyStorage(1_000, 100, 100);
        storage.setEnergy(300);
        assertEquals(100, storage.receiveEnergy(250, true));
        assertEquals(300, storage.getStoredEnergy());
        assertEquals(100, storage.extractEnergy(250, true));
        assertEquals(300, storage.getStoredEnergy());
    }

    @Test
    @DisplayName("Отдача ограничена maxOutput и запасом")
    void extractClamps() {
        EnergyStorage storage = new EnergyStorage(1_000, 100, 150);
        storage.setEnergy(400);
        assertEquals(150, storage.extractEnergy(999, false));
        assertEquals(250, storage.getStoredEnergy());
        storage.setEnergy(40);
        assertEquals(40, storage.extractEnergy(150, false), "нельзя отдать больше запаса");
        assertEquals(0, storage.getStoredEnergy());
        assertEquals(0, storage.extractEnergy(-5, false), "отрицательный запрос игнорируется");
        assertEquals(0, storage.receiveEnergy(0, false));
    }

    @Test
    @DisplayName("Мгновенная мощность отдачи: Дж/тик × 20")
    void outputPowerWatts() {
        EnergyStorage storage = new EnergyStorage(1_000_000, 50_000, 5_000);
        storage.setEnergy(20_000);
        assertEquals(100_000, storage.getOutputPowerWatts(), "5000 Дж/тик = 100 кВт");
        storage.setEnergy(1_000);
        assertEquals(20_000, storage.getOutputPowerWatts(), "запас ограничивает мощность");
    }

    @Test
    @DisplayName("Ёмкость и пределы не бывают отрицательными")
    void negativesRejected() {
        EnergyStorage storage = new EnergyStorage(-100, -10, -20);
        assertEquals(0, storage.getCapacity());
        assertEquals(0, storage.getMaxInput());
        assertEquals(0, storage.getMaxOutput());
        storage.setEnergy(-500);
        assertEquals(0, storage.getStoredEnergy());
        storage.setEnergy(10_000);
        assertEquals(0, storage.getStoredEnergy(), "ёмкость 0 — запаса нет");
    }

    @Test
    @DisplayName("1 кВт·ч = 3.6 МДж = 1 440 000 FE")
    void kwhConversion() {
        assertEquals(1_440_000, Units.joulesToFe(Units.JOULES_PER_KWH));
        assertEquals(Units.JOULES_PER_KWH, Units.feToJoules(1_440_000));
    }
}
