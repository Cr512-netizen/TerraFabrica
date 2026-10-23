package com.terrafabrica.content.shared.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Предмет-установщик рельсового транспорта: ставит сущность на рельсы.
 *
 * <p>Логика повторяет ванильную вагонетку: клик по блоку из тега
 * {@code minecraft:rails} создаёт сущность в центре блока с поправкой на уклон.
 */
public class RailVehicleItem extends Item {

    /** Фабрика сущности: (уровень, x, y, z) → сущность. */
    @FunctionalInterface
    public interface Factory {
        AbstractMinecart create(Level level, double x, double y, double z);
    }

    private final Factory factory;

    public RailVehicleItem(Factory factory, Properties properties) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel) {
            RailShape shape = state.getBlock() instanceof BaseRailBlock rail
                    ? rail.getRailDirection(state, level, pos, null)
                    : RailShape.NORTH_SOUTH;
            double yOffset = shape.isAscending() ? 0.5 : 0.0;
            AbstractMinecart vehicle = factory.create(serverLevel,
                    pos.getX() + 0.5, pos.getY() + 0.0625 + yOffset, pos.getZ() + 0.5);
            serverLevel.addFreshEntity(vehicle);
            serverLevel.gameEvent(GameEvent.ENTITY_PLACE, pos,
                    GameEvent.Context.of(context.getPlayer(), level.getBlockState(pos.below())));
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        com.terrafabrica.content.shared.ItemTooltips.append(stack, tooltip);
    }
}
