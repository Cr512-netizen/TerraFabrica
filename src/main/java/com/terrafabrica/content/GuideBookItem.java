package com.terrafabrica.content;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Внутриигровое руководство «Terra Fabrica» (собственное, без Patchouli).
 * ПКМ открывает экран руководства с главами; оттуда же можно перейти к дереву
 * исследований.
 *
 * <p>Клиентский экран открывается через {@code ClientGuideOpener}: прямая ссылка
 * на классы {@code net.minecraft.client.*} из общего кода роняет выделенный
 * сервер при загрузке класса (RuntimeDistCleaner).
 */
public class GuideBookItem extends Item {

    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
            com.terrafabrica.client.ClientGuideOpener.openGuide();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
