package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.server.*
import net.minecraft.server.level.*
import net.minecraft.world.level.*
import net.minecraft.world.level.storage.*

given ServerLevelAccessor Into ServerLevel = _.getLevel

extension(level: into[ServerLevel])
  def server: MinecraftServer = level.getServer
  def worldData: WorldData = server.getWorldData
  def levelSettings: LevelSettings = worldData.getLevelSettings
  def chunkSource: ServerChunkCache = level.getChunkSource
  def chunkGenerator: ChunkGenerator = chunkSource.getGenerator
  def noiseState: NoiseState = chunkSource.randomState
  def biomeSource: BiomeSource = chunkGenerator.getBiomeSource
