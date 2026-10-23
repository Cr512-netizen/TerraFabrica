package com.terrafabrica.client.screen;

import com.terrafabrica.content.shared.machine.AbstractMachineBlockEntity;
import com.terrafabrica.content.shared.machine.MachineLayout;
import com.terrafabrica.content.shared.machine.MachineMenu;
import com.terrafabrica.core.util.Units;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Единый экран GUI всех машин: энергия, прогресс, температура, RPM, момент,
 * мощность, баки флюидов и статус. Данные — из {@code ContainerData} меню;
 * флюиды — из {@code MachineMenu.clientFluids} (пакет сервера).
 * <p>
 * Раскладка задана в {@link MachineLayout} (она же используется конструктором меню и
 * генератором текстур), поэтому координаты здесь только читаются: полосы энергии и
 * баков 10x40 с x=8/22/36, слоты машины 2x2 слева и 2x2 справа, стрелка прогресса
 * (90,34) 16x6, одна строка телеметрии y=60, инвентарь игрока на ванильных местах.
 * <p>
 * Панель — ровно ванильные 176x166 и это принципиально: панель выше ванильной
 * вылезала за верхний и нижний край окна при большом масштабе интерфейса или маленьком
 * окне (игрок описал это как «GUI за экраном наполовину»). Плюс {@link #init()}
 * страхует позицию панели, если окно всё-таки меньше панели.
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("terrafabrica", "textures/gui/machine.png");

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = MachineLayout.GUI_WIDTH;
        imageHeight = MachineLayout.GUI_HEIGHT;
    }

    /**
     * После ванильного центрирования подтягиваем панель внутрь экрана. Если окно
     * меньше панели (маленькое окно, крупный масштаб интерфейса), ванильная формула
     * даёт отрицательные координаты и панель уходит за оба края — прижимаем её к
     * левому верхнему углу, чтобы хотя бы слоты и инвентарь оставались достижимы.
     */
    @Override
    protected void init() {
        super.init();
        if (leftPos < 0) {
            leftPos = 0;
        }
        if (topPos < 0) {
            topPos = 0;
        }
        if (leftPos + imageWidth > width) {
            leftPos = Math.max(0, width - imageWidth);
        }
        if (topPos + imageHeight > height) {
            topPos = Math.max(0, height - imageHeight);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Размер текстуры передаётся ЯВНО. Короткая перегрузка blit(файл, x, y, u, v, w, h)
        // считает текстуру 256x256 и растягивает панель 176x166 в 1.45x1.54 — из-за этого
        // рамки слотов на картинке не совпадали с настоящими слотами (тот самый «поехавший GUI»).
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0f, 0f, imageWidth, imageHeight,
                MachineLayout.GUI_WIDTH, MachineLayout.GUI_HEIGHT);

        var data = menu.data();
        // Энергия (долгота = запас/ёмкость; ёмкость хранится как максимум буфера).
        long energy = ((long) data.get(AbstractMachineBlockEntity.DATA_ENERGY_HI) << 32)
                | (data.get(AbstractMachineBlockEntity.DATA_ENERGY_LO) & 0xFFFFFFFFL);
        long capacity = menu.machine() != null ? menu.machine().energyStorage().getCapacity() : 0;
        if (capacity > 0) {
            int barHeight = (int) (MachineLayout.BAR_HEIGHT * (double) energy / capacity);
            graphics.fill(leftPos + MachineLayout.BAR_ENERGY_X,
                    topPos + MachineLayout.BAR_Y + MachineLayout.BAR_HEIGHT - barHeight,
                    leftPos + MachineLayout.BAR_ENERGY_X + MachineLayout.BAR_WIDTH,
                    topPos + MachineLayout.BAR_Y + MachineLayout.BAR_HEIGHT, 0xFFE03030);
        }
        // Прогресс (стрелка).
        int maxProgress = Math.max(1, data.get(AbstractMachineBlockEntity.DATA_MAX_PROGRESS));
        int progress = data.get(AbstractMachineBlockEntity.DATA_PROGRESS);
        if (progress > 0) {
            int widthArrow = (int) (MachineLayout.ARROW_WIDTH * (double) progress / maxProgress);
            graphics.fill(leftPos + MachineLayout.ARROW_X, topPos + MachineLayout.ARROW_Y,
                    leftPos + MachineLayout.ARROW_X + Math.max(1, widthArrow),
                    topPos + MachineLayout.ARROW_Y + MachineLayout.ARROW_HEIGHT, 0xFFE0E0E0);
        }
        // Баки флюидов (вход/выход).
        renderTank(graphics, leftPos + MachineLayout.BAR_TANK_IN_X, topPos + MachineLayout.BAR_Y,
                menu.clientFluids[0], data.get(AbstractMachineBlockEntity.DATA_FLUID_IN), 16_000);
        renderTank(graphics, leftPos + MachineLayout.BAR_TANK_OUT_X, topPos + MachineLayout.BAR_Y,
                menu.clientFluids[1], data.get(AbstractMachineBlockEntity.DATA_FLUID_OUT), 16_000);
    }

    private void renderTank(GuiGraphics graphics, int x, int y,
                            net.neoforged.neoforge.fluids.FluidStack fluid, int amount, int capacity) {
        int barHeight = (int) (MachineLayout.BAR_HEIGHT * (double) amount / capacity);
        if (barHeight > 0) {
            int color = fluid.isEmpty() ? 0xFF505050 : 0xFF3060E0;
            graphics.fill(x, y + MachineLayout.BAR_HEIGHT - barHeight,
                    x + MachineLayout.BAR_WIDTH, y + MachineLayout.BAR_HEIGHT, color);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        var data = menu.data();
        double tempK = data.get(AbstractMachineBlockEntity.DATA_TEMPERATURE_MK) / 1000.0;
        double rpm = data.get(AbstractMachineBlockEntity.DATA_RPM);
        double torque = data.get(AbstractMachineBlockEntity.DATA_TORQUE_CNM) / 100.0;
        double powerW = data.get(AbstractMachineBlockEntity.DATA_POWER_W);
        int status = data.get(AbstractMachineBlockEntity.DATA_STATUS);

        // Телеметрия — одной строкой под слотами: в 166 пикселей три строки не влезают
        // (они налезали на инвентарь игрока). Берём самый подробный вариант, который
        // влезает по ширине панели.
        graphics.drawString(font, telemetry(tempK, rpm, torque, powerW),
                leftPos + MachineLayout.INFO_X, topPos + MachineLayout.INFO_Y, 0xFF404040);

        // Статус — в строке заголовка справа, чтобы не занимать место над инвентарём.
        Component statusText = Component.translatable("status.terrafabrica." + status);
        graphics.drawString(font, statusText, leftPos + imageWidth - 8 - font.width(statusText),
                topPos + 6, 0xFF303030);
    }

    /**
     * Строка телеметрии: температура, обороты, момент и мощность. Форматы берутся из
     * языковых файлов, поэтому единицы измерения локализованы (кВт/kW, об/мин/RPM).
     * Вариант выбирается по фактической ширине текста, чтобы строка не наезжала на
     * рамку панели даже в русской локали.
     */
    private Component telemetry(double tempK, double rpm, double torque, double powerW) {
        String t = String.format("%.0f", Units.kelvinToCelsius(tempK));
        String n = String.format("%.0f", rpm);
        String m = String.format("%.0f", torque);
        String p = String.format(powerW >= 10_000 ? "%.1f" : "%.2f", powerW / 1000.0);
        int maxWidth = imageWidth - 2 * MachineLayout.INFO_X;
        Component wide = Component.translatable("gui.terrafabrica.telemetry", t, n, m, p);
        if (font.width(wide) <= maxWidth) {
            return wide;
        }
        Component medium = Component.translatable("gui.terrafabrica.telemetry.medium", t, n, p);
        if (font.width(medium) <= maxWidth) {
            return medium;
        }
        return Component.translatable("gui.terrafabrica.telemetry.compact", t, n, p);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
