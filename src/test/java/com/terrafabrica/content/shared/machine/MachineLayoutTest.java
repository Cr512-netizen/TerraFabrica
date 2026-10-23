package com.terrafabrica.content.shared.machine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Раскладка GUI машины: слоты, индикаторы и инвентарь игрока не должны пересекаться
 * и должны помещаться в панель {@code MachineLayout.GUI_WIDTH} x {@code GUI_HEIGHT}.
 *
 * <p>Тест появился после жалобы игрока «GUI машин поехал»: прежняя раскладка ставила
 * вход №4 и выход №1 в одну и ту же точку (98, 35) и налезала на стрелку прогресса,
 * а подписи телеметрии рисовались поверх слотов инвентаря. Ни компилятор, ни загрузка
 * игры такого не ловят — только глаза или вот такой тест.
 *
 * <p>Проверяется именно {@link MachineLayout}, потому что он не зависит от Minecraft:
 * координаты одинаковы для меню, экрана и генератора текстур.
 */
class MachineLayoutTest {

    @Test
    @DisplayName("Координаты слотов машин уникальны")
    void machineSlotsAreUnique() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < MachineLayout.SLOTS; i++) {
            String key = MachineLayout.slotX(i) + "," + MachineLayout.slotY(i);
            assertTrue(seen.add(key), "слот " + i + " совпал по координатам с другим слотом: " + key);
        }
        assertEquals(MachineLayout.SLOTS, seen.size());
    }

    @Test
    @DisplayName("Слоты машин идут двумя блоками 2x2: входы слева, выходы справа")
    void machineSlotsFormTwoBlocks() {
        for (int i = 0; i < MachineLayout.INPUT_SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            assertTrue(x < MachineLayout.ARROW_X, "вход " + i + " правее стрелки: x=" + x);
        }
        for (int i = MachineLayout.INPUT_SLOTS; i < MachineLayout.SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            assertTrue(x > MachineLayout.ARROW_X + MachineLayout.ARROW_WIDTH,
                    "выход " + i + " левее стрелки: x=" + x);
        }
    }

    @Test
    @DisplayName("Слоты машин лежат внутри панели с отступом от рамки")
    void machineSlotsInsidePanel() {
        for (int i = 0; i < MachineLayout.SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            int y = MachineLayout.slotY(i);
            assertTrue(x >= 8, "слот " + i + " прижат к левой рамке: x=" + x);
            assertTrue(y >= 8, "слот " + i + " прижат к верхней рамке: y=" + y);
            assertTrue(x + MachineLayout.SLOT_SIZE <= MachineLayout.GUI_WIDTH - 8,
                    "слот " + i + " выходит за правую рамку: x=" + x);
            assertTrue(y + MachineLayout.SLOT_SIZE <= MachineLayout.GUI_HEIGHT - 8,
                    "слот " + i + " выходит за нижнюю рамку: y=" + y);
        }
    }

    @Test
    @DisplayName("Слоты машин не пересекаются с полосами индикаторов и стрелкой")
    void machineSlotsClearIndicators() {
        for (int i = 0; i < MachineLayout.SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            int y = MachineLayout.slotY(i);
            for (int barX : new int[]{MachineLayout.BAR_ENERGY_X, MachineLayout.BAR_TANK_IN_X,
                    MachineLayout.BAR_TANK_OUT_X}) {
                assertFalse(overlaps(x, y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE,
                                barX, MachineLayout.BAR_Y, MachineLayout.BAR_WIDTH,
                                MachineLayout.BAR_HEIGHT),
                        "слот " + i + " налезает на индикатор x=" + barX);
            }
            assertFalse(overlaps(x, y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE,
                            MachineLayout.ARROW_X, MachineLayout.ARROW_Y, MachineLayout.ARROW_WIDTH,
                            MachineLayout.ARROW_HEIGHT),
                    "слот " + i + " налезает на стрелку прогресса");
        }
    }

    @Test
    @DisplayName("Слоты машин не пересекаются с инвентарём игрока")
    void machineSlotsClearPlayerInventory() {
        for (int i = 0; i < MachineLayout.SLOTS; i++) {
            int x = MachineLayout.slotX(i);
            int y = MachineLayout.slotY(i);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    assertFalse(overlaps(x, y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE,
                                    MachineLayout.INV_LEFT + col * MachineLayout.SLOT_SIZE,
                                    MachineLayout.INV_ROW_Y + row * MachineLayout.SLOT_SIZE,
                                    MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE),
                            "слот " + i + " налезает на инвентарь игрока (" + col + "," + row + ")");
                }
            }
            for (int col = 0; col < 9; col++) {
                assertFalse(overlaps(x, y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE,
                                MachineLayout.INV_LEFT + col * MachineLayout.SLOT_SIZE,
                                MachineLayout.HOTBAR_Y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE),
                        "слот " + i + " налезает на хотбар");
            }
        }
    }

    @Test
    @DisplayName("Телеметрия помещается между индикаторами и подписью инвентаря")
    void telemetryFitsAboveInventory() {
        int lineBottom = MachineLayout.INFO_Y + 9;
        assertTrue(lineBottom <= MachineLayout.INVENTORY_LABEL_Y,
                "строка телеметрии (" + lineBottom + ") налезает на подпись инвентаря ("
                        + MachineLayout.INVENTORY_LABEL_Y + ")");
        assertTrue(MachineLayout.INVENTORY_LABEL_Y + 10 <= MachineLayout.INV_ROW_Y,
                "подпись инвентаря налезает на его первый ряд");
        assertTrue(MachineLayout.INFO_Y >= MachineLayout.BAR_Y + MachineLayout.BAR_HEIGHT,
                "телеметрия налезает на полосы индикаторов");
        assertTrue(MachineLayout.SEPARATOR_Y > MachineLayout.INFO_Y + 9,
                "разделитель налезает на телеметрию");
        assertTrue(MachineLayout.SEPARATOR_Y < MachineLayout.INV_ROW_Y,
                "разделитель ниже первого ряда инвентаря");
    }

    @Test
    @DisplayName("Панель не больше ванильной: не вылезет за экран при любом масштабе, где влезает инвентарь")
    void panelFitsLikeVanillaInventory() {
        // Ванильный инвентарь: 176x166. Панель больше — риск уехать за край окна.
        assertEquals(176, MachineLayout.GUI_WIDTH);
        assertEquals(166, MachineLayout.GUI_HEIGHT);
        assertEquals(MachineLayout.INV_ROW_Y, 84, "инвентарь должен стоять на ванильной строке");
        assertEquals(MachineLayout.HOTBAR_Y, 142, "хотбар должен стоять на ванильной строке");
    }

    @Test
    @DisplayName("Инвентарь и хотбар целиком помещаются в панель")
    void playerInventoryFitsPanel() {
        int right = MachineLayout.INV_LEFT + 9 * MachineLayout.SLOT_SIZE;
        // Ванильный инвентарь занимает x=8..170 в панели 176 — справа остаётся 6 пикселей рамки.
        assertTrue(right <= MachineLayout.GUI_WIDTH - 6, "инвентарь шире панели: правый край " + right);
        int bottom = MachineLayout.HOTBAR_Y + MachineLayout.SLOT_SIZE;
        assertTrue(bottom <= MachineLayout.GUI_HEIGHT - 4, "хотбар выходит за панель: низ " + bottom);
        assertTrue(MachineLayout.HOTBAR_Y > MachineLayout.INV_ROW_Y,
                "хотбар должен быть ниже инвентаря");
    }

    private static boolean overlaps(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && bx < ax + aw && ay < by + bh && by < ay + ah;
    }
}
