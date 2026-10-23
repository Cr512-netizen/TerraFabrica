package com.terrafabrica.content.era1;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Блок породы — «камень с вкраплениями минерала».
 *
 * <p>Ключевое отличие Terra Fabrica от классических руд: добывается не руда,
 * а куски породы, которые затем дробятся и обогащаются. Свойства — как у камня:
 * добывается любой киркой; без кирки ничего не выпадает (loot table).
 *
 * <p>Варианты: минерал ({@code mineralId}), уровень богатства
 * ({@link PorodeRichness}) и основа (камень / глубинный сланец). Текстуры и
 * модели — собственные (не ванильные), задаются в assets.
 *
 * <p>Дропы — через loot table ({@code blocks/<id>.json}): Silk Touch даёт блок,
 * иначе — {@code baseChunks} кусков породы × (1 + Fortune).
 */
public class PorodeBlock extends Block {

    /** ID минерала (напр. {@code copper}). */
    private final String mineralId;
    /** Уровень богатства. */
    private final PorodeRichness richness;
    /** {@code true} — основа «глубинный сланец» (Y < 0). */
    private final boolean deepslate;

    public PorodeBlock(String mineralId, PorodeRichness richness, boolean deepslate) {
        super(BlockBehaviour.Properties.of()
                .sound(deepslate ? SoundType.DEEPSLATE : SoundType.STONE)
                .strength(deepslate ? 4.5F : 3.0F, 3.0F)
                .requiresCorrectToolForDrops());
        this.mineralId = mineralId;
        this.richness = richness;
        this.deepslate = deepslate;
    }

    public String mineralId() {
        return mineralId;
    }

    public PorodeRichness richness() {
        return richness;
    }

    public boolean deepslate() {
        return deepslate;
    }

    /** Полный id блока: {@code copper_porode_poor} / {@code deepslate_copper_porode_poor}. */
    public String blockId() {
        return idFor(mineralId, richness, deepslate);
    }

    /**
     * id блока без создания самого блока. Нужен при регистрации: объект блока
     * обязан создаваться лениво, внутри supplier'а {@code DeferredRegister},
     * иначе конструктор {@link Block} обращается к уже замороженному реестру
     * ({@code Registry is already frozen} во время инициализации мода).
     */
    public static String idFor(String mineralId, PorodeRichness richness, boolean deepslate) {
        return (deepslate ? "deepslate_" : "") + mineralId + "_porode_" + richness.suffix;
    }

    /** Кусок породы, выпадающий из блока: {@code copper_rock}. */
    public String rockItemId() {
        return mineralId + "_rock";
    }

    /**
     * Блоки пород регистрируются кодом (не через датапак), поэтому параметров
     * для сериализации нет — CODEC тождественный.
     */
    @Override
    protected MapCodec<? extends Block> codec() {
        return MapCodec.unit(this);
    }
}
