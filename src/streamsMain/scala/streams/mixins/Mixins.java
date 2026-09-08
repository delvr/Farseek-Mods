package streams.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import streams.*;

@Mixin(FlowingFluid.class)
abstract class FluidMixins {
    @WrapMethod(method = "getFlow")
    private Vec3 getFlow(BlockGetter blocks, BlockPos pos, FluidState state, Operation<Vec3> original) {
        return StreamsMod.fluidFlow(blocks, pos).orElse(original.call(blocks, pos, state));
    }
    @WrapMethod(method = "getHeight")
    private float getHeight(FluidState state, BlockGetter blocks, BlockPos pos, Operation<Float> original) {
        return StreamsMod.fluidHeight(blocks, pos).orElse(original.call(state, blocks, pos));
    }
}

@Mixin(Biome.class)
abstract class BiomeMixins {
    @WrapMethod(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z")
    private boolean shouldFreeze(LevelReader level, BlockPos pos, boolean edgesOnly, Operation<Boolean> original) {
        return original.call(level, pos, edgesOnly) && StreamsMod.fluidShouldFreeze(level, pos);
    }
}

@Mixin(TreeFeature.class)
abstract class TreeFeatureMixins {
    @WrapMethod(method = "validTreePos")
    private static boolean validTreePos(LevelSimulatedReader level, BlockPos pos, Operation<Boolean> original) {
        return original.call(level, pos) && StreamsMod.isValidTreePos(level, pos);
    }
}
