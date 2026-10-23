package com.terrafabrica.content.shared.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/**
 * Помощник проверки многоблочных структур.
 *
 * <p>Раскладка задаётся в локальных координатах «лицом на север»: x — вправо,
 * y — вверх, z — вперёд (от контроллера). При проверке раскладка поворачивается
 * по {@link MachineBlock#FACING} контроллера.
 *
 * <p>Пример (котёл 3×3×4, контроллер в центре нижнего слоя):
 * <pre>{@code
 * MultiblockHelper.matches(level, pos,
 *     MultiblockHelper.offset(-1, 0, -1, TFBlocks.STEAM_CASING.get()), ...);
 * }</pre>
 */
public final class MultiblockHelper {

    /** Элемент раскладки: смещение и предикат состояния блока. */
    public record Part(int dx, int dy, int dz, Predicate<BlockState> predicate) {

        public static Part of(int dx, int dy, int dz, net.minecraft.world.level.block.Block block) {
            return new Part(dx, dy, dz, state -> state.is(block));
        }

        public static Part of(int dx, int dy, int dz, Predicate<BlockState> predicate) {
            return new Part(dx, dy, dz, predicate);
        }

        /** Любой блок (заполнитель — не проверяется). */
        public static Part air(int dx, int dy, int dz) {
            return new Part(dx, dy, dz, state -> true);
        }
    }

    private MultiblockHelper() {}

    /**
     * Проверяет, что вокруг контроллера собрана полная структура.
     *
     * @param level      уровень
     * @param controller позиция контроллера
     * @param facing     направление контроллера
     * @param parts      раскладка в локальных координатах (x→право, y→вверх, z→вперёд)
     * @return {@code true}, если все блоки совпадают
     */
    public static boolean matches(Level level, BlockPos controller, Direction facing, List<Part> parts) {
        for (Part part : parts) {
            BlockPos world = localToWorld(controller, facing, part.dx(), part.dy(), part.dz());
            BlockState state = level.getBlockState(world);
            if (!part.predicate().test(state)) {
                return false;
            }
        }
        return true;
    }

    /** Проверка части структуры (например, вал сверху). */
    public static boolean matchesAny(Level level, BlockPos controller, Direction facing,
                                     List<Part> parts, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < Math.min(toIndex, parts.size()); i++) {
            Part part = parts.get(i);
            BlockPos world = localToWorld(controller, facing, part.dx(), part.dy(), part.dz());
            if (!part.predicate().test(level.getBlockState(world))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Преобразует локальные координаты (север = вперёд) в мировые с учётом
     * направления контроллера.
     */
    public static BlockPos localToWorld(BlockPos controller, Direction facing, int dx, int dy, int dz) {
        return switch (facing) {
            case NORTH -> controller.offset(dx, dy, dz);
            case SOUTH -> controller.offset(-dx, dy, -dz);
            case WEST -> controller.offset(dz, dy, -dx);
            case EAST -> controller.offset(-dz, dy, dx);
            default -> controller.offset(dx, dy, dz);
        };
    }

    /** Предикат «блок из списка». */
    public static Predicate<BlockState> anyOf(net.minecraft.world.level.block.Block... blocks) {
        return state -> {
            for (net.minecraft.world.level.block.Block block : blocks) {
                if (state.is(block)) return true;
            }
            return false;
        };
    }

    /** Предикат «любой блок» (кроме воздуха — структура не может быть пустой). */
    public static final Predicate<BlockState> NOT_AIR = state -> !state.isAir();

    /**
     * Заполненный параллелепипед W×H×D из одного блока, контроллер — в (0,0,0)
     * (нижний слой, по центру или слева для чётных размеров).
     */
    public static List<Part> solidBox(int width, int height, int depth, net.minecraft.world.level.block.Block block) {
        List<Part> parts = new java.util.ArrayList<>();
        int x0 = -(width - 1) / 2;
        for (int x = x0; x < x0 + width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // контроллер
                    parts.add(Part.of(x, y, z, block));
                }
            }
        }
        return parts;
    }
}
