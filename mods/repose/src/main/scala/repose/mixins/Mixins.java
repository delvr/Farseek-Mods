package repose.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockBehaviour.*;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.*;
import repose.*;

@Mixin(BlockStateBase.class)
abstract class BlockStateMixins {
    @WrapMethod(method = "getCollisionShape(" +
        "Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;" +
        "Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
    private VoxelShape getCollisionShape(BlockGetter blocks, BlockPos pos, CollisionContext context,
                                         Operation<VoxelShape> original) {
        return ReposeMod.shapeWithSlopes(pos, blocks, context, original.call(blocks, pos, context));
    }

    @WrapMethod(method = "isSuffocating")
    private boolean isSuffocating(BlockGetter blocks, BlockPos pos, Operation<Boolean> original) {
        return original.call(blocks, pos) && !ReposeMod.canSlope(pos, blocks);
    }
}
