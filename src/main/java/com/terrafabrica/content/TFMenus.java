package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.shared.machine.MachineMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Реестр меню. Для всех машин используется одно {@link MenuType} (отрисовка
 * различается только на клиенте); экран исследований — клиентский
 * {@code Screen} без контейнера (сервер проверяет действия пакетами).
 */
public final class TFMenus {

    private TFMenus() {}

    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE =
            TerraFabrica.MENUS.register("machine", () -> new MenuType<>(MachineMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** Вызывается из TerraFabrica; связывает MenuType с меню машин. */
    public static void init() {
        // Не MACHINE.get(): на этапе инициализации мода значение ещё не связано.
        MachineMenu.MachineMenuTypeHolder.bind(MACHINE);
    }
}
