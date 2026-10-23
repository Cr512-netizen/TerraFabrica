package com.terrafabrica.content.shared.machine;

import com.mojang.serialization.MapCodec;
import com.terrafabrica.TerraFabrica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Конкретный одноблочный станок/машина: вся логика — в BE, блок лишь задаёт
 * свойства и ориентацию.
 *
 * <p>BE-тип ищется в реестре {@code BLOCK_ENTITY_TYPE} по тому же id, что и у
 * блока (конвенция Terra Fabrica: {@code terrafabrica:crusher} → блок и BE).
 * Это убирает циклические зависимости инициализации между реестрами.
 */
public class SimpleMachineBlock extends MachineBlock {

    private final ResourceLocation blockEntityTypeId;

    public SimpleMachineBlock(BlockBehaviour.Properties properties, ResourceLocation blockEntityTypeId) {
        super(properties);
        this.blockEntityTypeId = blockEntityTypeId;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(blockEntityTypeId);
        return type != null ? type.create(pos, state) : null;
    }

    public static SimpleMachineBlock machine(String id) {
        return new SimpleMachineBlock(BlockBehaviour.Properties.of()
                .strength(3.5F, 6.0F)
                .requiresCorrectToolForDrops(),
                TerraFabrica.id(id));
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }
}
