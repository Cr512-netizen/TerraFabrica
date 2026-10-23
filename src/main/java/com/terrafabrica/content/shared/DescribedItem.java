package com.terrafabrica.content.shared;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Предмет с автоматическим тултипом {@code tooltip.<namespace>.<path>}. */
public class DescribedItem extends Item {

    public DescribedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ItemTooltips.append(stack, tooltip);
    }
}
