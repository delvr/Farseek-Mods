package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}

extension(chunk: Chunk)
  def pos: ChunkPos = chunk.getPos
  def pxz: ChunkXZ = pos.into[ChunkXZ]

given Chunk Into ChunkPos = _.pos
given Chunk Into ChunkXZ  = _.pxz

given Chunk    Into BlockXZBox = _.into[ChunkXZ].into[BlockXZBox]
given ChunkPos Into BlockXZBox = _.into[ChunkXZ].into[BlockXZBox]

given BlockPos Into ChunkXZ = _.into[BlockXZ].into[ChunkXZ]
given BlockXYZ Into ChunkXZ = _.into[BlockXZ].into[ChunkXZ]
