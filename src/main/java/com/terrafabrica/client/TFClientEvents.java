package com.terrafabrica.client;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.client.hud.TFHud;
import com.terrafabrica.client.screen.MachineScreen;
import com.terrafabrica.content.TFFluids;
import com.terrafabrica.content.TFMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.Map;

/**
 * Клиентские события: экраны машин, HUD-слой и текстуры жидкостей.
 */
@EventBusSubscriber(modid = TerraFabrica.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class TFClientEvents {

    private TFClientEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TFMenus.MACHINE.get(), MachineScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(TerraFabrica.id("hud"), TFHud::render);
    }

    /**
     * Текстуры жидкостей. Без этого все 22 флюида рисовались бы ванильной
     * водой ({@code minecraft:block/water_still}) — кислоту, нефть и расплав
     * было бы не отличить ни в мире, ни в баках машин.
     *
     * <p>Цвет не тинтуется: {@code textures/block/fluid/<id>_still.png} уже
     * нарисованы в нужной палитре (`tools/generate_textures.py`), поэтому
     * {@link IClientFluidTypeExtensions#getTintColor()} остаётся белым.
     */
    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        for (Map.Entry<String, ?> entry : TFFluids.TYPES.entrySet()) {
            String id = entry.getKey();
            ResourceLocation still = TerraFabrica.id("block/fluid/" + id + "_still");
            ResourceLocation flowing = TerraFabrica.id("block/fluid/" + id + "_flow");
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return still;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowing;
                }
            }, TFFluids.TYPES.get(id));
        }
    }
}
