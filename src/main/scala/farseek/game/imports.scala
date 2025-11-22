package farseek.game

/** [[https://docs.scala-lang.org/scala3/reference/other-new-features/export.html]] */
object imports:
  // Minecraft
  export net.minecraft.client.multiplayer.ClientLevel
  export net.minecraft.core.{BlockPos, Direction, Holder, Registry, RegistryAccess}
  export net.minecraft.core.registries.Registries.*
  export net.minecraft.resources.{ResourceLocation, ResourceKey}
  export net.minecraft.server.level.{ServerLevel, WorldGenRegion}
  export net.minecraft.tags.TagKey
  export net.minecraft.util.RandomSource
  export net.minecraft.world.entity.{Entity, EntityType, LivingEntity, Mob}
  export net.minecraft.world.entity.player.Player
  export net.minecraft.world.item.{Item, Items, ItemStack}
  export net.minecraft.world.item.enchantment.Enchantment
  export net.minecraft.world.level.{ChunkPos, LevelHeightAccessor, WorldGenLevel}
  export net.minecraft.world.level.biome.{Biome, Biomes, BiomeManager, BiomeSource}
  export net.minecraft.world.level.block.{Block, Blocks}
  export net.minecraft.world.level.block.entity.BlockEntity
  export net.minecraft.world.level.block.state.BlockState
  export net.minecraft.world.level.material.{Fluid, Fluids, FluidState}
  export net.minecraft.world.level.{BlockGetter, Level, LevelAccessor, LevelReader, LevelWriter}
  export net.minecraft.world.level.chunk.{ChunkAccess as Chunk, ChunkGenerator, LevelChunk, ProtoChunk}
  export net.minecraft.world.level.chunk.status.ChunkStatus
  export net.minecraft.world.level.levelgen.{Heightmap, NoiseBasedChunkGenerator, NoiseChunk as NoiseArea, RandomState as NoiseState}
  export net.minecraft.world.level.levelgen.Heightmap.Types as HeightmapType
  export net.minecraft.world.level.levelgen.feature.Feature
  export net.minecraft.world.level.levelgen.structure.{Structure, StructureStart, StructurePiece, BoundingBox as StructureBoundingBox}
  export net.minecraft.world.phys.{AABB, Vec3, HitResult, BlockHitResult, EntityHitResult}
  export net.minecraft.world.phys.shapes.VoxelShape
  // NeoForged
  export net.neoforged.bus.api.{Event, IEventBus, SubscribeEvent}
  export net.neoforged.fml.ModContainer
  export net.neoforged.fml.common.Mod
  export net.neoforged.neoforge.common.CommonHooks.*
  export net.neoforged.neoforge.common.NeoForge.*
  export net.neoforged.neoforge.registries.{DeferredRegister, RegisterEvent}
  export net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.*
