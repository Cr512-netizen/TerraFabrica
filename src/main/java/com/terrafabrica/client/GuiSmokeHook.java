package com.terrafabrica.client;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.client.screen.GuideScreen;
import com.terrafabrica.client.screen.MachineScreen;
import com.terrafabrica.client.screen.ResearchScreen;
import com.terrafabrica.content.shared.machine.MachineMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Автоматический смоук клиентских экранов — только для разработки.
 *
 * <p>Включается системным свойством {@code -Dterrafabrica.guismoke=true} (готовый
 * запуск: {@code gradle --offline clientSmoke}). Зачем нужно: экраны руководства и
 * древа исследований не покрываются ни юнит-тестами, ни GameTest — они рисуются
 * только в живом клиенте, а проверка «глазами» не автоматизируется. Смоук открывает
 * экран по-настоящему (конструктор + {@code init} + несколько кадров отрисовки),
 * снимает скриншот в {@code run/screenshots/} и завершает игру.
 *
 * <p>Последовательность по тикам клиента: 40 — руководство, 60 — скриншот,
 * 90 — древо исследований, 110 — скриншот, 120 — выход. Любое исключение при
 * открытии или отрисовке валит игру с отчётом, то есть смоук падает громко.
 *
 * <p>В обычной игре хука нет: без свойства класс ничего не делает.
 */
@EventBusSubscriber(modid = TerraFabrica.MODID, value = Dist.CLIENT)
public final class GuiSmokeHook {

    private static final boolean ENABLED = Boolean.getBoolean("terrafabrica.guismoke");
    /** Мир, в который заходит смоук (папка в {@code run/saves}). */
    private static final String WORLD = "tfsmoke";
    /** Сколько тиков ждать загрузки мира перед тем, как сдаться. */
    private static final int WAIT_LIMIT = 2400;
    private static final int OPEN_GUIDE = 40;
    private static final int SHOT_GUIDE = 60;
    private static final int OPEN_RESEARCH = 80;
    private static final int SHOT_RESEARCH = 100;
    private static final int OPEN_MACHINE = 120;
    private static final int SHOT_MACHINE = 140;
    private static final int QUIT = 150;

    private static int waited;
    private static int ticks;
    private static boolean worldRequested;

    private GuiSmokeHook() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        // Машинный экран нельзя открыть без игрока: AbstractContainerScreen в тике читает
        // minecraft.player.isAlive(). Поэтому сначала просим клиент зайти в мир
        // (аргумент --quickPlaySingleplayer этим билдом не поддерживается, проверено),
        // и только потом начинаем отсчёт шагов.
        if (minecraft.player == null) {
            if (!worldRequested) {
                worldRequested = true;
                TerraFabrica.LOGGER.info("[guismoke] открываю мир {}", WORLD);
                minecraft.createWorldOpenFlows().openWorld(WORLD, () -> TerraFabrica.LOGGER.error(
                        "[guismoke] мир run/saves/{} не открылся: создайте его "
                                + "(см. docs/testing.md, раздел про смоук)", WORLD));
            }
            if (++waited > WAIT_LIMIT) {
                TerraFabrica.LOGGER.warn("[guismoke] мир не загрузился за {} тиков — выход", WAIT_LIMIT);
                minecraft.stop();
            }
            return;
        }
        ticks++;
        switch (ticks) {
            case OPEN_GUIDE -> {
                TerraFabrica.LOGGER.info("[guismoke] открываю руководство");
                minecraft.setScreen(new GuideScreen());
            }
            case SHOT_GUIDE -> shot(minecraft, "guide");
            case OPEN_RESEARCH -> {
                TerraFabrica.LOGGER.info("[guismoke] открываю древо исследований");
                minecraft.setScreen(new ResearchScreen());
            }
            case SHOT_RESEARCH -> shot(minecraft, "research");
            case OPEN_MACHINE -> {
                TerraFabrica.LOGGER.info("[guismoke] открываю GUI машины");
                openMachine(minecraft);
            }
            case SHOT_MACHINE -> shot(minecraft, "machine");
            case QUIT -> {
                TerraFabrica.LOGGER.info("[guismoke] экраны открылись без исключений, выходим");
                minecraft.stop();
            }
            default -> { }
        }
    }

    /**
     * Машинный экран без мира: клиентский конструктор меню сам создаёт слоты-заглушки,
     * поэтому GUI можно открыть прямо на титульном экране и проверить его раскладку.
     */
    private static void openMachine(Minecraft minecraft) {
        Inventory inventory = minecraft.player != null
                ? minecraft.player.getInventory()
                : new Inventory(null);
        MachineMenu menu = new MachineMenu(0, inventory);
        minecraft.setScreen(new MachineScreen(menu, inventory, Component.literal("Дробилка")));
    }

    private static void shot(Minecraft minecraft, String name) {
        var screen = minecraft.screen;
        TerraFabrica.LOGGER.info("[guismoke] снимок {}: экран={}, gui={}x{}",
                name,
                screen == null ? "нет" : screen.getClass().getSimpleName(),
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight());
        if (screen instanceof AbstractContainerScreen<?> container) {
            // Геометрия панели: если guiLeft/guiTop ушли в минус или панель не по центру —
            // это и есть «GUI поехал», видно по числам без разглядывания картинки.
            TerraFabrica.LOGGER.info(
                    "[guismoke]   панель: guiLeft={} guiTop={} размер={}x{} окно={}x{} масштаб={}",
                    container.getGuiLeft(), container.getGuiTop(),
                    container.getXSize(), container.getYSize(),
                    minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(),
                    minecraft.options.guiScale().get());
            // Панель обязана целиком лежать в пределах экрана: «GUI за экраном» ловится здесь,
            // а не глазами. Это же условие — причина держать панель ванильного размера 176x166.
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            boolean onScreen = container.getGuiLeft() >= 0 && container.getGuiTop() >= 0
                    && container.getGuiLeft() + container.getXSize() <= width
                    && container.getGuiTop() + container.getYSize() <= height;
            TerraFabrica.LOGGER.info("[guismoke]   панель целиком на экране: {} ({}+{}<={}, {}+{}<={})",
                    onScreen ? "да" : "НЕТ",
                    container.getGuiLeft(), container.getXSize(), width,
                    container.getGuiTop(), container.getYSize(), height);
            // Координаты слотов: сверяем с рамками в текстуре панели. Слоты обязаны стоять
            // ровно в рамках (входы 50/68 x 18/36, выходы 110/128, инвентарь 8+18c x 84/102/120/142).
            StringBuilder slots = new StringBuilder();
            for (int i = 0; i < container.getMenu().slots.size(); i++) {
                var slot = container.getMenu().slots.get(i);
                if (i > 0) {
                    slots.append(' ');
                }
                slots.append(i).append("=(").append(slot.x).append(',').append(slot.y).append(')');
            }
            TerraFabrica.LOGGER.info("[guismoke]   слоты({}): {}", container.getMenu().slots.size(), slots);
            if (container.getMenu() instanceof MachineMenu machine) {
                TerraFabrica.LOGGER.info("[guismoke]   MachineMenu: machine={} данные={}",
                        machine.machine() == null ? "клиент-заглушка" : "BE",
                        machine.data().getCount());
            }
        }
        Screenshot.grab(minecraft.gameDirectory, "terrafabrica_" + name + ".png",
                minecraft.getMainRenderTarget(), component -> { });
    }
}
