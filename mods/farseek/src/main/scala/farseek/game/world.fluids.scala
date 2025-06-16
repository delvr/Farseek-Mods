package farseek.game

import farseek.game.imports.*
import net.minecraft.world.level.block.state.properties.BlockStateProperties.*

val FluidLevel = IntProperty.positive[FluidState](LEVEL_FLOWING)

val FallingFluid = BoolProperty[FluidState](FALLING)
