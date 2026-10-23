package com.terrafabrica.content.shared.machine;

/**
 * Геометрия GUI машины: единственный источник правды для
 * {@code MachineMenu} (координаты слотов), {@code MachineScreen} (индикаторы,
 * телеметрия) и {@code tools/generate_textures.py} (текстура панели).
 *
 * <p>Класс намеренно не зависит от классов Minecraft: благодаря этому раскладку
 * можно проверять юнит-тестом {@code MachineLayoutTest} без запуска игры. Именно
 * такой тест и появился после жалобы игрока «GUI машин поехал»: прежняя раскладка
 * ставила вход №4 и выход №1 в одну точку (98, 35) и налезала на стрелку прогресса,
 * а заметить это можно было только глазами.
 *
 * <p>Размер панели — ровно ванильные 176x166. Это не косметика: панель выше ванильной
 * (промежуточная версия была 190) вылезает за верхний и нижний край экрана на малом
 * окне или при увеличенном масштабе интерфейса, потому что
 * {@code AbstractContainerScreen} центрирует панель без обрезки и не подстраивает её
 * под высоту окна. Теперь панель влезает везде, где влезает ванильный инвентарь.
 *
 * <p>Вторая половина той же жалобы («наполовину за экраном») оказалась не в размере:
 * {@code MachineScreen} рисовал панель короткой перегрузкой
 * {@code GuiGraphics.blit(файл, x, y, u, v, w, h)}, которая считает текстуру 256x256.
 * Текстура 176x166 из-за этого растягивалась в 1.45x1.54: рамки слотов уезжали от
 * настоящих слотов, а нижняя часть (инвентарь) обрезалась краем панели. Поэтому размер
 * текстуры передаётся в {@code blit} явно; {@code MachinePanelTextureTest} следит, чтобы
 * PNG оставался того же размера, что и константы ниже.
 *
 * <p>Раскладка (в игровых пикселях от левого верхнего угла панели):
 * <pre>
 *   y=6    заголовок (слева) и статус (справа)
 *   y=18   полосы: энергия x=8, бак входа x=22, бак выхода x=36 (10x40)
 *   y=18   входы  2x2: (50,18) (68,18) / (50,36) (68,36)
 *   y=34   стрелка прогресса: (90,34) 16x6
 *   y=18   выходы 2x2: (110,18) (128,18) / (110,36) (128,36)
 *   y=60   одна строка телеметрии: «T °C · RPM · P кВт»
 *   y=72   подпись «Инвентарь» (ванильная формула imageHeight - 94)
 *   y=82   линия-разделитель
 *   y=84   инвентарь игрока (3x9), y=142 хотбар — ванильные координаты
 * </pre>
 */
public final class MachineLayout {

    /** Размер панели: ванильные 176x166, чтобы панель никогда не вылезала за экран. */
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;

    /** Слоты машины: 4 входа и 4 выхода. */
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 4;
    public static final int SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;

    /** Сторона спрайта слота вместе с рамкой. */
    public static final int SLOT_SIZE = 18;
    /** Левый край колонки слотов инвентаря и хотбара (ванильный). */
    public static final int INV_LEFT = 8;
    /** Верх первой строки инвентаря игрока и верх хотбара (ванильные). */
    public static final int INV_ROW_Y = 84;
    public static final int HOTBAR_Y = 142;
    /** Подпись инвентаря: ванильная формула {@code imageHeight - 94}. */
    public static final int INVENTORY_LABEL_Y = GUI_HEIGHT - 94;
    /** Линия-разделитель между зоной машины и инвентарём. */
    public static final int SEPARATOR_Y = 82;

    /** Полосы индикаторов (энергия и два бака). */
    public static final int BAR_Y = 18;
    public static final int BAR_HEIGHT = 40;
    public static final int BAR_WIDTH = 10;
    public static final int BAR_ENERGY_X = 8;
    public static final int BAR_TANK_IN_X = 22;
    public static final int BAR_TANK_OUT_X = 36;

    /** Стрелка прогресса между блоками слотов. */
    public static final int ARROW_X = 90;
    public static final int ARROW_Y = 34;
    public static final int ARROW_WIDTH = 16;
    public static final int ARROW_HEIGHT = 6;

    /** Строка телеметрии под слотами (одна, чтобы не залезать в инвентарь). */
    public static final int INFO_X = 8;
    public static final int INFO_Y = 60;

    /** Верх блока слотов машины и координаты колонок. */
    private static final int SLOT_TOP = 18;
    private static final int INPUT_COLUMN_X = 50;
    private static final int OUTPUT_COLUMN_X = 110;

    private MachineLayout() {}

    /** X слота машины внутри панели. */
    public static int slotX(int index) {
        int column = index % 2;
        return index < INPUT_SLOTS
                ? INPUT_COLUMN_X + SLOT_SIZE * column
                : OUTPUT_COLUMN_X + SLOT_SIZE * column;
    }

    /** Y слота машины внутри панели. */
    public static int slotY(int index) {
        return SLOT_TOP + SLOT_SIZE * ((index % INPUT_SLOTS) / 2);
    }
}
