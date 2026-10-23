package com.terrafabrica.client;

import com.terrafabrica.client.screen.GuideScreen;
import net.minecraft.client.Minecraft;

/**
 * Клиентский мост для открытия руководства.
 *
 * <p>Класс существует отдельно, потому что общий код (предмет
 * {@code GuideBookItem}) не имеет права ссылаться на клиентские классы
 * напрямую: при загрузке класса на выделенном сервере верификатор подтягивает
 * иерархию {@code Screen}, и {@code RuntimeDistCleaner} падает с
 * «Attempted to load class net/minecraft/client/gui/screens/Screen for invalid
 * dist DEDICATED_SERVER». Вызов отсюда разрешается только в ветке
 * {@code level.isClientSide}, то есть на сервере этот класс не загружается.
 */
public final class ClientGuideOpener {

    private ClientGuideOpener() {}

    /** Открывает экран руководства (только на клиенте). */
    public static void openGuide() {
        Minecraft instance = Minecraft.getInstance();
        if (instance != null) {
            instance.setScreen(new GuideScreen());
        }
    }
}
