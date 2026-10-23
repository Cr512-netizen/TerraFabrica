package com.terrafabrica.core.api.heat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Теплосодержание Q = C·T в джоулях и кельвинах.
 *
 * <p>Печь не может нагреться выше {@code maxTemperature} и не может остыть ниже
 * температуры среды (20 °C), поэтому «отрицательного тепла» в системе не возникает.
 */
class HeatStorageTest {

    private static final double EPS = 1e-6;

    @Test
    @DisplayName("Q = C·T, старт при температуре среды")
    void initialHeat() {
        HeatStorage storage = new HeatStorage(1_000, 1_500, 2.0);
        assertEquals(HeatStorage.AMBIENT_TEMPERATURE_K, storage.getTemperature(), EPS);
        assertEquals(293.15, storage.getTemperature(), EPS);
        assertEquals(293.15 * 1_000, storage.getHeatEnergy(), EPS);
        assertEquals(1_000, storage.getHeatCapacity(), EPS);
        assertEquals(1_500, storage.getMaxTemperature(), EPS);
    }

    @Test
    @DisplayName("Добавление тепла поднимает температуру на Q/C")
    void addHeatRaisesTemperature() {
        HeatStorage storage = new HeatStorage(2_000, 2_000, 5.0);
        assertEquals(2_000, storage.addHeat(2_000, false), EPS, "принято ровно 2000 Дж");
        assertEquals(HeatStorage.AMBIENT_TEMPERATURE_K + 1.0, storage.getTemperature(), EPS);
    }

    @Test
    @DisplayName("Нагрев ограничен maxTemperature, лишнее не принимается")
    void addHeatClampsAtMax() {
        HeatStorage storage = new HeatStorage(100, 500, 1.0);
        double toMax = 500 * 100 - 293.15 * 100;   // до 500 K
        assertEquals(toMax, storage.addHeat(toMax * 10, false), 1e-3);
        assertEquals(500, storage.getTemperature(), EPS);
        assertEquals(0, storage.addHeat(50, false), EPS, "печь уже на пределе");
    }

    @Test
    @DisplayName("Охлаждение не опускает температуру ниже температуры среды")
    void removeHeatFloorsAtAmbient() {
        HeatStorage storage = new HeatStorage(100, 2_000, 1.0);
        storage.setTemperature(400);
        double available = (400 - HeatStorage.AMBIENT_TEMPERATURE_K) * 100;
        assertEquals(available, storage.removeHeat(available * 10, false), 1e-3);
        assertEquals(HeatStorage.AMBIENT_TEMPERATURE_K, storage.getTemperature(), EPS);
        assertEquals(0, storage.removeHeat(100, false), EPS, "ниже среды не остывает");
    }

    @Test
    @DisplayName("simulate не меняет температуру")
    void simulateKeepsState() {
        HeatStorage storage = new HeatStorage(500, 1_800, 3.0);
        storage.setTemperature(1_000);
        storage.addHeat(5_000, true);
        storage.removeHeat(5_000, true);
        storage.coolToAmbient(true);
        assertEquals(1_000, storage.getTemperature(), EPS);
    }

    @Test
    @DisplayName("Пассивное остывание за тик пропорционально перегреву")
    void coolingPerTick() {
        HeatStorage hot = new HeatStorage(10_000, 2_000, 4.0);
        hot.setTemperature(1_293.15);   // на 1000 K выше среды
        double lost = hot.coolToAmbient(false);
        // Q = h·ΔT·dt = 4 · 1000 · (1/20) = 200 Дж
        assertEquals(200, lost, 1e-6);
        assertTrue(hot.getTemperature() < 1_293.15);

        HeatStorage cold = new HeatStorage(10_000, 2_000, 4.0);
        assertEquals(0, cold.coolToAmbient(false), EPS, "при температуре среды остывать некуда");
    }

    @Test
    @DisplayName("setTemperature ограничен диапазоном [среда, max]")
    void setTemperatureClamps() {
        HeatStorage storage = new HeatStorage(1_000, 1_873, 1.0);
        storage.setTemperature(5_000);
        assertEquals(1_873, storage.getTemperature(), EPS);
        storage.setTemperature(-50);
        assertEquals(HeatStorage.AMBIENT_TEMPERATURE_K, storage.getTemperature(), EPS);
    }

    @Test
    @DisplayName("Плавка меди требует 1085 °C, доменный предел 1500 °C")
    void furnaceTemperatures() {
        HeatStorage bloomery = new HeatStorage(50_000, 1_573.15, 8.0);   // 1300 °C
        HeatStorage blast = new HeatStorage(200_000, 1_773.15, 20.0);    // 1500 °C
        assertEquals(1_573.15, bloomery.getMaxTemperature(), EPS);
        assertEquals(1_773.15, blast.getMaxTemperature(), EPS);
        assertEquals(1_085, com.terrafabrica.core.util.Units.kelvinToCelsius(1_358.15), 1e-6);
    }
}
