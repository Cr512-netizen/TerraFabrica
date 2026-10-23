package com.terrafabrica.content.shared.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Базовый блок всех машин: горизонтальная ориентация, свойство LIT (активная
 * анимация), открытие GUI по ПКМ и выпадение содержимого при разрушении.
 */
public abstract class MachineBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected MachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * Запоминает владельца (поставившего игрока): по нему машина проверяет
     * исследования для гейтированных рецептов, пока GUI никто не открывал.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
            machine.setOpener(serverPlayer.getUUID());
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Тикер машины. Без него блоки-сущности не тикают вообще: ванильный
     * {@code Level.tickBlockEntities()} вызывает только те BE, у которых есть
     * {@link BlockEntityTicker}. Клиенту тик не нужен — анимации идут по
     * состоянию {@code LIT}, которое синхронизирует сервер.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof AbstractMachineBlockEntity machine) {
                machine.tickServer();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        // Shift+ПКМ по редуктору — переключение передаточного отношения.
        if (player.isShiftKeyDown() && be instanceof com.terrafabrica.content.era1.GearboxBlockEntity gearbox) {
            if (!level.isClientSide) {
                gearbox.cycleRatio();
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Ratio: " + gearbox.ratio()),
                        true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && be instanceof AbstractMachineBlockEntity machine
                && player instanceof ServerPlayer serverPlayer) {
            machine.openGui(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        // Ведро воды наполняет входной бак машины.
        if (be instanceof AbstractMachineBlockEntity machine && stack.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
            int filled = machine.fill(new net.neoforged.neoforge.fluids.FluidStack(
                    net.minecraft.world.level.material.Fluids.WATER, 1000),
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0 && !player.getAbilities().instabuild) {
                stack.shrink(1);
                var empty = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET);
                if (!player.getInventory().add(empty)) {
                    player.drop(empty, false);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        // Открытие GUI по ПКМ любым предметом.
        return useWithoutItem(state, level, pos, player, hitResult) == InteractionResult.sidedSuccess(level.isClientSide)
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
                machine.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
