package com.terrafabrica.content.shared.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Строительный блок-обшивка многоблочных конструкций (котлы, домны, реакторы).
 * Отличается только звуком/прочностью/цветом; логики не имеет.
 */
public class CasingBlock extends Block {

    public CasingBlock(SoundType soundType, float hardness, float resistance) {
        super(BlockBehaviour.Properties.of()
                .sound(soundType)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops());
    }

    /** Обшивки регистрируются кодом — параметров для сериализации нет. */
    @Override
    protected MapCodec<? extends Block> codec() {
        return MapCodec.unit(this);
    }
}
