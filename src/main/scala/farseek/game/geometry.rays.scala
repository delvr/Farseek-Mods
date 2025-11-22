package farseek.game

import farseek.game.imports.*
import farseek.util.*
import net.minecraft.world.phys.*

given EmptyTest[HitResult] = _.getType == HitResult.Type.MISS

extension(result: HitResult)
  def hitPos: EuclidXYZ = result.getLocation.assumedAbsolute

extension(result: BlockHitResult)
  def hitBlockPos: BlockXYZ = result.getBlockPos.converted
  def hitBlockSide: XYZSide = result.getDirection.converted
