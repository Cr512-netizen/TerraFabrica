package com.terrafabrica.content.shared;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Тултипы предметов по соглашению об именах.
 *
 * <p>Ключ выводится из {@code getDescriptionId()}: для
 * {@code item.terrafabrica.steam_locomotive} ищется
 * {@code tooltip.terrafabrica.steam_locomotive}. Отсутствующий ключ не
 * добавляет пустую строку — проверка через {@link Component#getString()} не
 * нужна, так как пустых значений в локализации нет (см.
 * {@code tests/DataPackIntegrityTest}).
 */
public final class ItemTooltips {

    private ItemTooltips() {
    }

    public static void appendVehicle(ItemStack stack, List<Component> tooltip) {
        append(stack, tooltip);
    }

    public static void append(ItemStack stack, List<Component> tooltip) {
        String descriptionId = stack.getItem().getDescriptionId();
        String key = descriptionId.startsWith("item.")
                ? "tooltip." + descriptionId.substring("item.".length())
                : "tooltip." + descriptionId;
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
}
