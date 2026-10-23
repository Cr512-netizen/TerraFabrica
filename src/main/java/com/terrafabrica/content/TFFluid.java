package com.terrafabrica.content;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * Базовый класс флюидов Terra Fabrica (NeoForge {@link BaseFlowingFluid}).
 * Используются в основном в баках машин; в мире размещать не обязательно.
 */
public abstract class TFFluid extends BaseFlowingFluid {

    protected TFFluid(Properties properties) {
        super(properties);
    }

    /** Источник (статичный уровень 8). */
    public static class Source extends TFFluid {

        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    /** Текущий (свойство LEVEL). */
    public static class Flowing extends TFFluid {

        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
