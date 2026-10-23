package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.era1.PorodeBlock;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Реестр предметов Terra Fabrica.
 *
 * <p>Породы: {@code <mineral>_rock} (кусок породы), {@code <mineral>_powder}
 * (порошок), {@code <mineral>_concentrate} (концентрат). Инструменты — бронза и
 * железо (молот, ножницы, напильник, зубило, клещи, пила + стандартный набор).
 * BlockItem'ы регистрируются автоматически для всех блоков {@link TFBlocks}.
 */
public final class TFItems {

    private TFItems() {}

    // ------------------------------------------------------------------
    // Материалы пород: <mineral>_rock / _powder / _concentrate
    // ------------------------------------------------------------------

    public static final Map<String, DeferredItem<Item>> MATERIALS = new LinkedHashMap<>();

    static {
        for (String mineral : TFBlocks.ERA1_MINERALS) {
            registerMineral(mineral);
        }
        for (String mineral : TFBlocks.ERA2_MINERALS) {
            registerMineral(mineral);
        }
    }

    private static void registerMineral(String mineral) {
        MATERIALS.put(mineral + "_rock", item(mineral + "_rock"));
        MATERIALS.put(mineral + "_powder", item(mineral + "_powder"));
        MATERIALS.put(mineral + "_concentrate", item(mineral + "_concentrate"));
    }

    /** {@code <mineral>_rock} — кусок породы (не плавится, требует дробления). */
    public static DeferredItem<Item> rock(String mineral) {
        return MATERIALS.get(mineral + "_rock");
    }

    /** {@code <mineral>_powder} — порошок после дробилки. */
    public static DeferredItem<Item> powder(String mineral) {
        return MATERIALS.get(mineral + "_powder");
    }

    /** {@code <mineral>_concentrate} — концентрат после сепаратора. */
    public static DeferredItem<Item> concentrate(String mineral) {
        return MATERIALS.get(mineral + "_concentrate");
    }

    // ------------------------------------------------------------------
    // Металлы и сплавы
    // ------------------------------------------------------------------

    public static final DeferredItem<Item> TIN_INGOT = item("tin_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = item("bronze_ingot");
    public static final DeferredItem<Item> LEAD_INGOT = item("lead_ingot");
    public static final DeferredItem<Item> PIG_IRON_INGOT = item("pig_iron_ingot");   // чугун
    public static final DeferredItem<Item> BRASS_INGOT = item("brass_ingot");         // латунь (Эпоха II, цинк)
    public static final DeferredItem<Item> STEEL_INGOT = item("steel_ingot");
    public static final DeferredItem<Item> STAINLESS_STEEL_INGOT = item("stainless_steel_ingot");
    public static final DeferredItem<Item> SUPERALLOY_INGOT = item("superalloy_ingot");

    public static final DeferredItem<Item> ALUMINUM_INGOT = item("aluminum_ingot");
    public static final DeferredItem<Item> CHROMIUM_INGOT = item("chromium_ingot");
    public static final DeferredItem<Item> NICKEL_INGOT = item("nickel_ingot");
    public static final DeferredItem<Item> ZINC_INGOT = item("zinc_ingot");
    public static final DeferredItem<Item> MOLYBDENUM_INGOT = item("molybdenum_ingot");
    public static final DeferredItem<Item> TUNGSTEN_INGOT = item("tungsten_ingot");
    public static final DeferredItem<Item> MANGANESE_INGOT = item("manganese_ingot");
    public static final DeferredItem<Item> MAGNESIUM_INGOT = item("magnesium_ingot");
    public static final DeferredItem<Item> PLATINUM_INGOT = item("platinum_ingot");
    public static final DeferredItem<Item> COBALT_INGOT = item("cobalt_ingot");
    public static final DeferredItem<Item> NIOBIUM_INGOT = item("niobium_ingot");
    public static final DeferredItem<Item> TITANIUM_INGOT = item("titanium_ingot");
    public static final DeferredItem<Item> VANADIUM_INGOT = item("vanadium_ingot");

    // ------------------------------------------------------------------
    // Промышленные материалы и химикаты
    // ------------------------------------------------------------------

    public static final DeferredItem<Item> SLAG = item("slag");                       // шлак
    public static final DeferredItem<Item> COKE = item("coke");                       // кокс
    public static final DeferredItem<Item> COAL_TAR = item("coal_tar");               // смола
    public static final DeferredItem<Item> LIME = item("lime");                       // известь
    public static final DeferredItem<Item> POTASH = item("potash");                   // поташ
    public static final DeferredItem<Item> SODA_ASH = item("soda_ash");               // сода
    public static final DeferredItem<Item> SALTPETER = item("saltpeter");             // селитра
    public static final DeferredItem<Item> AMMONIUM_CHLORIDE = item("ammonium_chloride");
    public static final DeferredItem<Item> CAUSTIC_SODA = item("caustic_soda");
    public static final DeferredItem<Item> YELLOWCAKE = item("yellowcake");           // жёлтый кек
    public static final DeferredItem<Item> CRYOLITE = item("cryolite");
    public static final DeferredItem<Item> FLUORIDE_SALT = item("fluoride_salt");     // уловленный фтор
    public static final DeferredItem<Item> AMMONIUM_NITRATE = item("ammonium_nitrate");
    public static final DeferredItem<Item> SUPERPHOSPHATE = item("superphosphate");
    public static final DeferredItem<Item> POTASH_FERTILIZER = item("potash_fertilizer");
    public static final DeferredItem<Item> SULFURIC_ACID_BOTTLE = item("sulfuric_acid_bottle");
    public static final DeferredItem<Item> NITRIC_ACID_BOTTLE = item("nitric_acid_bottle");
    public static final DeferredItem<Item> HYDROCHLORIC_ACID_BOTTLE = item("hydrochloric_acid_bottle");
    public static final DeferredItem<Item> GRAPHITE_ELECTRODE = item("graphite_electrode");
    public static final DeferredItem<Item> FIREBRICK = item("firebrick");
    public static final DeferredItem<Item> SALT = item("salt");
    public static final DeferredItem<Item> SODIUM_BICARBONATE = item("sodium_bicarbonate");
    public static final DeferredItem<Item> BRONZE_GEAR = item("bronze_gear");
    public static final DeferredItem<Item> IRON_GEAR = item("iron_gear");
    public static final DeferredItem<Item> AMMONIA_BOTTLE = item("ammonia_bottle");
    public static final DeferredItem<Item> TITANIUM_ALLOY_INGOT = item("titanium_alloy_ingot");
    public static final DeferredItem<Item> STEEL_BEAM = item("steel_beam");

    // Катализаторы (с прочностью: изнашиваются в реакторах).
    public static final DeferredItem<Item> IRON_CATALYST =
            item("iron_catalyst", () -> new Item(new Item.Properties().durability(600)));
    public static final DeferredItem<Item> PLATINUM_RHODIUM_NET =
            item("platinum_rhodium_net", () -> new Item(new Item.Properties().durability(1_000)));
    public static final DeferredItem<Item> VANADIUM_CATALYST =
            item("vanadium_catalyst", () -> new Item(new Item.Properties().durability(800)));

    // ------------------------------------------------------------------
    // Детали машин
    // ------------------------------------------------------------------

    public static final DeferredItem<Item> IRON_ROD = item("iron_rod");               // вал
    public static final DeferredItem<Item> BOLT = item("bolt");
    public static final DeferredItem<Item> NUT = item("nut");
    public static final DeferredItem<Item> PRECISION_PART = item("precision_part");   // деталь 0.5 мм
    public static final DeferredItem<Item> MACHINE_FRAME = item("machine_frame");
    public static final DeferredItem<Item> PISTON = item("piston");
    public static final DeferredItem<Item> CYLINDER = item("cylinder");
    public static final DeferredItem<Item> CONDENSER = item("condenser");
    public static final DeferredItem<Item> BALANCE_BEAM = item("balance_beam");       // балансир
    public static final DeferredItem<Item> PUMP = item("pump");
    public static final DeferredItem<Item> BOILER_PART = item("boiler_part");
    public static final DeferredItem<Item> FLYWHEEL = item("flywheel");
    public static final DeferredItem<Item> STEAM_CHEST = item("steam_chest");         // золотник
    public static final DeferredItem<Item> GOVERNOR = item("governor");               // регулятор
    public static final DeferredItem<Item> BELLOWS = item("bellows");                 // меха

    public static final DeferredItem<Item> COPPER_WIRE = item("copper_wire");
    public static final DeferredItem<Item> ALUMINUM_WIRE = item("aluminum_wire");
    public static final DeferredItem<Item> STEEL_WIRE = item("steel_wire");
    public static final DeferredItem<Item> COPPER_COIL = item("copper_coil");
    public static final DeferredItem<Item> COPPER_PLATE = item("copper_plate");
    public static final DeferredItem<Item> IRON_PLATE = item("iron_plate");
    public static final DeferredItem<Item> STEEL_PLATE = item("steel_plate");
    public static final DeferredItem<Item> ALUMINUM_PLATE = item("aluminum_plate");
    public static final DeferredItem<Item> TIN_PLATE = item("tin_plate");
    public static final DeferredItem<Item> BRONZE_PLATE = item("bronze_plate");
    public static final DeferredItem<Item> ROTOR = item("rotor");
    public static final DeferredItem<Item> STATOR = item("stator");
    public static final DeferredItem<Item> VACUUM_TUBE = item("vacuum_tube");         // лампа
    public static final DeferredItem<Item> MAGNETIC_CORE = item("magnetic_core");
    public static final DeferredItem<Item> CARBON_MICROPHONE = item("carbon_microphone");
    public static final DeferredItem<Item> SPEAKER = item("speaker");
    public static final DeferredItem<Item> COMMUTATOR_BOARD = item("commutator_board");
    public static final DeferredItem<Item> ANTENNA = item("antenna");
    public static final DeferredItem<Item> WHEEL = item("wheel");                     // колесо
    public static final DeferredItem<Item> WAGON_BODY = item("wagon_body");
    public static final DeferredItem<Item> RUBBER = item("rubber");

    // ------------------------------------------------------------------
    // Приборы (HUD/датчики) и руководство
    // ------------------------------------------------------------------

    public static final DeferredItem<Item> MULTIMETER = item("multimeter");
    public static final DeferredItem<Item> THERMOMETER = item("thermometer");
    public static final DeferredItem<Item> TACHOMETER = item("tachometer");
    public static final DeferredItem<Item> GEIGER_COUNTER = item("geiger_counter");
    public static final DeferredItem<Item> GUIDE_BOOK =
            TerraFabrica.ITEMS.register("guide_book", () -> new GuideBookItem(new Item.Properties().stacksTo(1)));

    // ------------------------------------------------------------------
    // Инструменты
    // ------------------------------------------------------------------

    /** Бронзовый тир инструментов. */
    public static final Tier BRONZE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 220;
        }

        @Override
        public float getSpeed() {
            return 6.5F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 1.5F;
        }

        @Override
        public int getEnchantmentValue() {
            return 12;
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.INCORRECT_FOR_STONE_TOOL;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(BRONZE_INGOT.get());
        }
    };

    public static final DeferredItem<Item> BRONZE_PICKAXE =
            item("bronze_pickaxe", () -> new PickaxeItem(BRONZE_TIER, toolProps(1, -2.8F)));
    public static final DeferredItem<Item> BRONZE_AXE =
            item("bronze_axe", () -> new AxeItem(BRONZE_TIER, toolProps(6, -3.1F)));
    public static final DeferredItem<Item> BRONZE_SHOVEL =
            item("bronze_shovel", () -> new ShovelItem(BRONZE_TIER, toolProps(1.5F, -3.0F)));
    public static final DeferredItem<Item> BRONZE_HOE =
            item("bronze_hoe", () -> new HoeItem(BRONZE_TIER, toolProps(-2, -1.0F)));
    public static final DeferredItem<Item> BRONZE_HAMMER =
            item("bronze_hammer", () -> new TieredItem(BRONZE_TIER, toolProps(5, -3.0F)));
    public static final DeferredItem<Item> BRONZE_SHEARS =
            item("bronze_shears", () -> new net.minecraft.world.item.ShearsItem(toolProps().durability(220)));
    public static final DeferredItem<Item> BRONZE_FILE =
            item("bronze_file", () -> new TieredItem(BRONZE_TIER, toolProps(2, -2.4F)));
    public static final DeferredItem<Item> BRONZE_CHISEL =
            item("bronze_chisel", () -> new TieredItem(BRONZE_TIER, toolProps(3, -2.6F)));
    /** Клещи: бронзовый аналог {@link #IRON_TONGS} (нужны для горячего металла). */
    public static final DeferredItem<Item> BRONZE_TONGS =
            item("bronze_tongs", () -> new TieredItem(BRONZE_TIER, toolProps(1, -2.0F)));
    public static final DeferredItem<Item> BRONZE_SAW =
            item("bronze_saw", () -> new TieredItem(BRONZE_TIER, toolProps(2, -2.6F)));

    public static final DeferredItem<Item> IRON_HAMMER =
            item("iron_hammer", () -> new TieredItem(net.minecraft.world.item.Tiers.IRON, toolProps(6, -3.0F)));
    public static final DeferredItem<Item> IRON_FILE =
            item("iron_file", () -> new TieredItem(net.minecraft.world.item.Tiers.IRON, toolProps(2, -2.4F)));
    public static final DeferredItem<Item> IRON_CHISEL =
            item("iron_chisel", () -> new TieredItem(net.minecraft.world.item.Tiers.IRON, toolProps(3, -2.6F)));
    public static final DeferredItem<Item> IRON_TONGS =
            item("iron_tongs", () -> new TieredItem(net.minecraft.world.item.Tiers.IRON, toolProps(1, -2.0F)));
    public static final DeferredItem<Item> IRON_SAW =
            item("iron_saw", () -> new TieredItem(net.minecraft.world.item.Tiers.IRON, toolProps(2, -2.6F)));

    // ------------------------------------------------------------------
    // Транспорт (предметы-сущности)
    // ------------------------------------------------------------------

    public static final DeferredItem<Item> MINECART = item("minecart",
            () -> new net.minecraft.world.item.MinecartItem(net.minecraft.world.entity.vehicle.AbstractMinecart.Type.RIDEABLE,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> STEAM_LOCOMOTIVE = item("steam_locomotive",
            () -> new com.terrafabrica.content.shared.transport.RailVehicleItem(
                    com.terrafabrica.content.era1.SteamLocomotiveEntity::new,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DIESEL_LOCOMOTIVE = item("diesel_locomotive",
            () -> new com.terrafabrica.content.shared.transport.RailVehicleItem(
                    com.terrafabrica.content.era2.DieselLocomotiveEntity::new,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CAR = item("car");
    public static final DeferredItem<Item> SHIP = item("ship");
    public static final DeferredItem<Item> SUBMARINE = item("submarine");

    /** Канистра дизеля: 12 л топлива, заправляет тепловоз. */
    public static final int DIESEL_CANISTER_MB = 12_000;
    public static final DeferredItem<Item> DIESEL_CANISTER = item("diesel_canister",
            () -> new com.terrafabrica.content.shared.DescribedItem(new Item.Properties().stacksTo(8)));

    // ------------------------------------------------------------------
    // Регистрация
    // ------------------------------------------------------------------

    private static Item.Properties toolProps(float attackBonus, float speedModifier) {
        return new Item.Properties()
                .attributes(net.minecraft.world.item.DiggerItem.createAttributes(BRONZE_TIER, attackBonus, speedModifier));
    }

    private static Item.Properties toolProps() {
        return new Item.Properties();
    }

    private static DeferredItem<Item> item(String id) {
        return TerraFabrica.ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> item(String id, Supplier<Item> factory) {
        return TerraFabrica.ITEMS.register(id, factory);
    }

    /** Инициализация: регистрация BlockItem'ов для всех блоков (кроме флюидов). */
    public static void init() {
        for (var entry : TerraFabrica.BLOCKS.getEntries()) {
            String id = entry.getId().getPath();
            if (TerraFabrica.ITEMS.getEntries().stream().anyMatch(e -> e.getId().getPath().equals(id))) {
                continue;
            }
            // Предмет регистрируется лениво: на этом этапе объекты блоков ещё не
            // созданы (реестр блоков замораживается позже), поэтому BlockItem
            // строится внутри supplier'а, когда блок уже связан.
            // Жидкостные блоки сюда не попадают: они регистрируются в TFFluids
            // после TFItems.init, а предметной формы у флюидов нет.
            TerraFabrica.ITEMS.register(id, () -> {
                Block block = entry.get();
                return block instanceof PorodeBlock
                        ? new PorodeBlockItem(block, new Item.Properties())
                        : new BlockItem(block, new Item.Properties());
            });
        }
    }

    /**
     * BlockItem породы: тултип «Требует дробления и обогащения» —
     * кусок породы нельзя переплавить напрямую.
     */
    public static final class PorodeBlockItem extends BlockItem {

        public PorodeBlockItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void appendHoverText(net.minecraft.world.item.ItemStack stack,
                                    net.minecraft.world.item.Item.TooltipContext context,
                                    java.util.List<net.minecraft.network.chat.Component> tooltip,
                                    net.minecraft.world.item.TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.terrafabrica.porode_block"));
        }
    }
}
