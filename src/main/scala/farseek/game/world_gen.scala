package farseek.game

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.world.level.*
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.Aquifer.FluidPicker
import net.minecraft.world.level.levelgen.DensityFunctions.*
import net.minecraft.world.level.levelgen.blending.*
import net.minecraft.world.level.levelgen.feature.*
import net.minecraft.world.level.levelgen.feature.configurations.*
import net.minecraft.world.level.levelgen.structure.*
import net.minecraft.world.level.levelgen.structure.Structure.*
import net.minecraft.world.level.levelgen.structure.StructureStart.*
import net.minecraft.world.level.levelgen.structure.pieces.*
import net.minecraft.world.level.levelgen.structure.templatesystem.*
import net.minecraft.world.level.levelgen.synth.*

// World Gen:
// https://minecraft.wiki/w/World_generation
// https://minecraft.wiki/w/Custom_world_generation
// https://minecraft.wiki/w/Tutorials/Custom_world_generation
// https://gist.github.com/delvr/bfac43cd48675ec74b905ffe640a6a11
// Minecraft terrain generation in a nutshell by Henrik Kniberg https://www.youtube.com/watch?v=CSa5O6knuwI
// Minecraft 1.18 prepping a snapshot & explaining how stuff works by Henrik Kniberg https://www.youtube.com/watch?v=TycBrFKEteU
// https://www.mcjty.eu/docs/1.18/ep5#custom-dimension

// Features:
// https://minecraft.wiki/w/Placed_feature
// https://minecraft.wiki/w/Configured_feature
// https://docs.neoforged.net/docs/worldgen/biomemodifier

// Structures:
// https://minecraft.wiki/w/Structure_definition
// https://minecraft.wiki/w/Tutorials/Custom_structures
// https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.21-Neoforge-Jigsaw
// https://gist.github.com/GentlemanRevvnar/98a8f191f46d28f63592672022c41497

abstract class NoConfigFeature extends Feature(NoneFeatureConfiguration.CODEC):
  def placeAt(p: ChunkXZ)(using WorldGenLevel, ChunkGenerator, RandomSource): Unit
  final override def place(context: FeaturePlaceContext[NoneFeatureConfiguration]): Boolean =
    import context.*
    placeAt(origin.converted)(using level, chunkGenerator, random)
    true

final case class PreBuildSurface(generator: NoiseBasedChunkGenerator, region: WorldGenRegion,
    structureManager: StructureManager, noise: NoiseState, chunk: ProtoChunk)
  extends CancellableEvent

final case class PostBuildSurface(generator: NoiseBasedChunkGenerator, region: WorldGenRegion,
    structureManager: StructureManager, noise: NoiseState, chunk: ProtoChunk)
  extends Event

object BuildSurface:
  def shouldProceed(generator: NoiseBasedChunkGenerator, region: WorldGenRegion, structureManager: StructureManager,
      noise: NoiseState, chunk: ProtoChunk): Boolean =
    PreBuildSurface(generator, region, structureManager, noise, chunk).postThenCheck().shouldProceed

  def postBuild(generator: NoiseBasedChunkGenerator, region: WorldGenRegion, structureManager: StructureManager,
      noise: NoiseState, chunk: ProtoChunk): Unit =
    PostBuildSurface(generator, region, structureManager, noise, chunk).post()

final case class PlaceFeature[F <: Feature[C], C <: FeatureConfiguration](feature: F, config: C, here: BlockPos,
    level: WorldGenLevel, generator: ChunkGenerator, randomGen: RandomSource)
  extends CancellableEvent

object PlaceFeature:
  def shouldProceed[F <: Feature[C], C <: FeatureConfiguration](feature: F, config: C,
      level: WorldGenLevel, generator: ChunkGenerator, randomGen: RandomSource, here: BlockPos): Boolean =
    PlaceFeature(feature, config, here, level, generator, randomGen).postThenCheck().shouldProceed

final case class GenerateStructure(structure: Structure, pieces: MutableISeq[StructurePiece], context: GenerationContext)
  extends Event

object GenerateStructure:
  def generate(structure: Structure, registries: RegistryAccess, chunks: ChunkGenerator, biomes: BiomeSource,
      noise: NoiseState, templates: StructureTemplateManager, seed: Long, pos: ChunkPos, references: Int,
      level: LevelHeightAccessor, biomeFilter: JPredicate[Holder[Biome]]): StructureStart =
    val context = GenerationContext(registries, chunks, biomes, noise, templates, seed, pos, level, biomeFilter)
    val start = structure.findValidGenerationPoint(context).toScala.map: generator =>
      val pieces = MutableISeq.from(generator.getPiecesBuilder.build.pieces.asScala)
      GenerateStructure(structure, pieces, context).post()
      StructureStart(structure, pos, references, PiecesContainer(pieces.asJava))
    start.filter(_.isValid).getOrElse(INVALID_START)

private val SingleOctave = Seq(Integer.valueOf(0)).asJava

class NoiseGenerator(scale: into[PositiveReal], seed: Long):
  private val noise = PerlinSimplexNoise(WorldgenRandom(LegacyRandomSource(seed)), SingleOctave)
  def negToPosOneAt(p: into[BlockXZ]): `[-1,1]` =
    `[-1,1]`(noise.getValue(p.x.toInt/scale, p.z.toInt/scale, false))
  def zeroToOneAt(p: into[BlockXZ]): `[0,1]` =
    `[0,1]`(fma(negToPosOneAt(p), 0.5, 0.5))
  def zeroOrOneAt(p: into[BlockXZ], chanceOfOne: Probability = HalfChance): NonNegativeInt =
    if trueAt(p, chanceOfOne) then `1` else `0`
  def trueAt(p: into[BlockXZ], probability: Probability = HalfChance): Boolean =
    zeroToOneAt(p) <= probability

given (generator: NoiseBasedChunkGenerator) => NoiseGeneratorSettings = generator.settings

given (settings: NoiseGeneratorSettings) => NoiseSettings = settings.noiseSettings

given (settings: NoiseSettings) => LevelHeightAccessor = settings.heights

extension(settings: NoiseSettings)
  def heights: LevelHeightAccessor = new LevelHeightAccessor:
    override val getMinY   = settings.minY
    override val getHeight = settings.height

extension(box: BlockYBox)
  def heights: LevelHeightAccessor = new LevelHeightAccessor:
    override val getMinY   = box.min.toInt
    override val getHeight = box.length

extension(generator: NoiseBasedChunkGenerator)
  def settings: NoiseGeneratorSettings = generator.generatorSettings.value

  def noiseColumnAt(p: into[BlockXZ])(using noise: NoiseState): NoiseColumn =
    generator.getBaseColumn(p.x.toInt, p.z.toInt, generator.settings.noiseSettings.heights, noise)

  def noiseArea(min: into[BlockXZ], width: PositiveInt)(using noise: NoiseState,
      settings: NoiseGeneratorSettings = generator.settings,
      beardifier: BeardifierMarker = BeardifierMarker.INSTANCE,
      fluidPicker: FluidPicker = generator.globalFluidPicker.get,
      blender: Blender = Blender.empty): NoiseArea =
    val noiseSettings = settings.noiseSettings
    val cellWidth = noiseSettings.getCellWidth
    NoiseChunk(width / cellWidth, noise,
      floorDiv(min.x.toInt, cellWidth) * cellWidth,
      floorDiv(min.z.toInt, cellWidth) * cellWidth,
      noiseSettings, beardifier, settings, fluidPicker, blender)

given Applicable[NoiseColumn, BlockY, BlockState] = (column, y) => column.getBlock(y.toInt)
