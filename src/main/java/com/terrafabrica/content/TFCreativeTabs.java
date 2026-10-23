package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Креативная вкладка Terra Fabrica (все предметы и блоки мода).
 */
public final class TFCreativeTabs {

    private TFCreativeTabs() {}

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TerraFabrica.CREATIVE_TABS.register("terrafabrica", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.terrafabrica"))
                    .icon(() -> new ItemStack(TFItems.GUIDE_BOOK.get()))
                    .displayItems((parameters, output) -> {
                        for (var entry : TerraFabrica.ITEMS.getEntries()) {
                            output.accept(new ItemStack(entry.get()));
                        }
                    })
                    .build());

    public static void init() {
    }
}
