package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}

extension(chunk: Chunk)
  def pos: ChunkPos = chunk.getPos
  def pxz: ChunkXZ = pos.converted

given Chunk Into ChunkPos = _.pos
given Chunk Into ChunkXZ  = _.pxz

given Chunk    Into BlockXZBox = _.chunkXZ.converted
given ChunkPos Into BlockXZBox = _.chunkXZ.converted
