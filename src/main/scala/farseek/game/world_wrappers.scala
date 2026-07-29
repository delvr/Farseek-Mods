package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.levelgen.Heightmap

trait WrappedHeights extends BlockGetter:
  protected def wrapped: LevelHeightAccessor
  final override lazy val getMinY  : Int = wrapped.getMinY
  final override lazy val getHeight: Int = wrapped.getHeight

trait ChunkGetter extends WrappedHeights:
  def chunkAt(p: into[ChunkXZ]): Chunk
  override def getBlockState (p: BlockPos): BlockState  = chunkAt(p).getBlockState(p)
  override def getFluidState (p: BlockPos): FluidState  = chunkAt(p).getFluidState(p)
  override def getBlockEntity(p: BlockPos): BlockEntity = chunkAt(p).getBlockEntity(p)

trait BoundedBlockGetter extends WrappedHeights:
  def xzBounds: BlockXZBox
  final protected def withValidated[T](p: BlockPos)(f: BlockPos => T): T =
    { p.blockXYZ.validateIn(this.xyzBounds); f(p) }
  protected def blockStateAtValidated (p: BlockPos): BlockState
  protected def fluidStateAtValidated (p: BlockPos): FluidState
  protected def blockEntityAtValidated(p: BlockPos): BlockEntity
  final override def getBlockState (p: BlockPos): BlockState  = withValidated(p)(blockStateAtValidated)
  final override def getFluidState (p: BlockPos): FluidState  = withValidated(p)(fluidStateAtValidated)
  final override def getBlockEntity(p: BlockPos): BlockEntity = withValidated(p)(blockEntityAtValidated)

object BoundedBlockGetter:
  given BoundedBlockGetter Into BlockXZBox = _.xzBounds

trait BoundedChunkGetter extends ChunkGetter, BoundedBlockGetter:
  final def chunkBounds: ChunkXZBox = xzBounds.converted
  protected def chunkAtValidated(p: ChunkXZ): Chunk
  override def chunkAt(p: into[ChunkXZ]): Chunk = chunkAtValidated(p.validatedIn(chunkBounds))
  override protected def blockStateAtValidated (p: BlockPos): BlockState  = chunkAt(p).getBlockState(p)
  override protected def fluidStateAtValidated (p: BlockPos): FluidState  = chunkAt(p).getFluidState(p)
  override protected def blockEntityAtValidated(p: BlockPos): BlockEntity = chunkAt(p).getBlockEntity(p)

trait WrappedBlockGetter extends BoundedBlockGetter:
  override protected def wrapped: BlockGetter
  override protected def blockStateAtValidated (p: BlockPos): BlockState  = wrapped.getBlockState(p)
  override protected def fluidStateAtValidated (p: BlockPos): FluidState  = wrapped.getFluidState(p)
  override protected def blockEntityAtValidated(p: BlockPos): BlockEntity = wrapped.getBlockEntity(p)

object WrappedBlockGetter:
  def apply(getter: BlockGetter, center: into[BlockXZ], range: NonNegativeInt = `1`) = new WrappedBlockGetter:
    override protected lazy val wrapped = getter
    override lazy val xzBounds = center.bufferBox(range)

trait WrappedLevelReader extends LevelReader, BoundedChunkGetter, WrappedBlockGetter:
  override protected def wrapped: LevelReader
  final override lazy val dimensionType   = wrapped.dimensionType
  final override lazy val enabledFeatures = wrapped.enabledFeatures
  final override lazy val getBiomeManager = wrapped.getBiomeManager
  final override lazy val getLightEngine  = wrapped.getLightEngine
  final override lazy val getSeaLevel     = wrapped.getSeaLevel
  final override lazy val getSkyDarken    = wrapped.getSkyDarken
  final override lazy val getWorldBorder  = wrapped.getWorldBorder
  final override lazy val isClientSide    = wrapped.isClientSide
  final override lazy val registryAccess  = wrapped.registryAccess
  final override lazy val environmentAttributes = wrapped.environmentAttributes
  final override protected def chunkAtValidated(p: ChunkXZ): Chunk =
    wrapped.getChunk(p.x.toInt, p.z.toInt)
  final override def getChunk(chunkX: Int, chunkZ: Int, status: ChunkStatus, nullable: Boolean): Chunk =
    chunkAt((ChunkX(chunkX), ChunkZ(chunkZ)))
  final override def hasChunk(chunkX: Int, chunkZ: Int): Boolean =
    wrapped.hasChunk(chunkX, chunkZ): @nowarn
  final override def getHeight(heightType: Heightmap.Types, blockX: Int, blockZ: Int): Int =
    wrapped.getHeight(heightType, blockX, blockZ)
  final override def getEntityCollisions(entity: Entity, box: AABB): JList[VoxelShape] =
    wrapped.getEntityCollisions(entity, box)
  final override def getUncachedNoiseBiome(quartX: Int, quartY: Int, quartZ: Int): Holder[Biome] =
    wrapped.getUncachedNoiseBiome(quartX, quartY, quartZ)

object WrappedLevelReader:
  def apply(level: LevelReader, center: into[BlockXZ], range: NonNegativeInt = `1`) = new WrappedLevelReader:
    override protected lazy val wrapped = level
    override lazy val xzBounds = center.bufferBox(range)

trait WrappedLevelReaderWriter extends LevelReader, LevelWriter, WrappedLevelReader:
  override protected def wrapped: LevelReader & LevelWriter
  final override def setBlock(p: BlockPos, state: BlockState, updateFlags: Int, updateLimit: Int): Boolean =
    withValidated(p)(wrapped.setBlock(_, state, updateFlags, updateLimit))
  final override def removeBlock(p: BlockPos, movedByPiston: Boolean): Boolean =
    withValidated(p)(wrapped.removeBlock(_, movedByPiston))
  final override def destroyBlock(p: BlockPos, dropResources: Boolean, destroyer: Entity, updateLimit: Int): Boolean =
    withValidated(p)(wrapped.destroyBlock(_, dropResources, destroyer, updateLimit))

object WrappedLevelReaderWriter:
  def apply(level: LevelReader & LevelWriter, center: into[BlockXZ], range: NonNegativeInt = `1`) = new WrappedLevelReaderWriter:
    override protected lazy val wrapped = level
    override lazy val xzBounds = center.bufferBox(range)
