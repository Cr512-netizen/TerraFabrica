package com.terrafabrica;

import com.mojang.logging.LogUtils;
import com.terrafabrica.content.TFAttachments;
import com.terrafabrica.content.TFBlockEntities;
import com.terrafabrica.content.TFBlocks;
import com.terrafabrica.content.TFCreativeTabs;
import com.terrafabrica.content.TFFluids;
import com.terrafabrica.content.TFItems;
import com.terrafabrica.content.TFMenus;
import com.terrafabrica.content.TFRecipeSerializers;
import com.terrafabrica.content.TFRecipeTypes;
import com.terrafabrica.content.TFSounds;
import com.terrafabrica.config.TFClientConfig;
import com.terrafabrica.config.TFServerConfig;
import com.terrafabrica.core.research.ResearchTree;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

/**
 * Terra Fabrica — научно-технический прогрессия-мод для NeoForge 1.21.1.
 *
 * <p>Охват: Эпоха I (Механическое пробуждение) и Эпоха II (Промышленная революция).
 * Все физические величины — в СИ (Дж, Вт, K, Па, Н, В, А); внутренняя единица энергии — джоуль.
 *
 * <p>Архитектура пакетов:
 * <ul>
 *   <li>{@code core.api} — публичный API (энергия, тепло, вращение, исследования);</li>
 *   <li>{@code core.*} — реализации хранилищ и вспомогательные утилиты;</li>
 *   <li>{@code content.era1}, {@code content.era2} — машины и блоки Эпох;</li>
 *   <li>{@code content.shared} — общий каркас машин;</li>
 *   <li>{@code client} — GUI, HUD, рендер, партиклы;</li>
 *   <li>{@code data} — датаген (рецепты, теги, worldgen, модели);</li>
 *   <li>{@code config} — серверные/клиентские настройки (ModConfigSpec).</li>
 * </ul>
 */
@Mod(TerraFabrica.MODID)
public final class TerraFabrica {

    /** Mod ID / namespace: {@code terrafabrica}. */
    public static final String MODID = "terrafabrica";

    /** Логгер мода. */
    public static final Logger LOGGER = LogUtils.getLogger();

    // ---------------------------------------------------------------------
    // Регистры. Содержимое — в content.* (TFBlocks, TFItems, ...).
    // ---------------------------------------------------------------------
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public TerraFabrica(IEventBus modBus, ModContainer container) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        SOUND_EVENTS.register(modBus);
        FLUIDS.register(modBus);
        FLUID_TYPES.register(modBus);
        CREATIVE_TABS.register(modBus);
        ATTACHMENTS.register(modBus);

        // Инициализация содержимого (статические поля регистров).
        TFBlocks.init();
        TFItems.init();
        TFBlockEntities.init();
        TFMenus.init();
        TFRecipeTypes.init();
        TFRecipeSerializers.init();
        TFSounds.init();
        TFFluids.init();
        TFCreativeTabs.init();
        TFAttachments.init();
        ResearchTree.init();

        // Конфигурация: серверная (силовые параметры, генерация) и клиентская (HUD, тултипы).
        container.registerConfig(ModConfig.Type.SERVER, TFServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, TFClientConfig.SPEC);

        LOGGER.info("Terra Fabrica loaded (Eras I & II).");
    }

    /** Создаёт {@link ResourceLocation} в неймспейсе мода. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static String idString(String path) {
        return MODID + ":" + path;
    }
}
