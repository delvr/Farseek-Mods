package farseek.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import farseek.game.*;
import java.util.function.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.spongepowered.asm.mixin.*;

// https://github.com/SpongePowered/Mixin/
// https://github.com/LlamaLad7/MixinExtras
// https://fabricmc.net/wiki/tutorial:mixin_tips
// https://github.com/Sinytra/Connector/discussions/383
// https://discord.com/channels/313125603924639766/733055378371117127

@Mixin(NoiseBasedChunkGenerator.class)
abstract class ChunkGeneratorMixins {
    @WrapMethod(method = "buildSurface(" +
        "Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;" +
        "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
    private void buildSurface(WorldGenRegion region, StructureManager manager,
                              RandomState noise, ChunkAccess chunkAccess, Operation<Void> original) {
        NoiseBasedChunkGenerator generator = cast(this);
        ProtoChunk chunk = (ProtoChunk)chunkAccess;
        if(BuildSurface.shouldProceed(generator, region, manager, noise, chunk))
            original.call(region, manager, noise, chunk);
        BuildSurface.postBuild(generator, region, manager, noise, chunk);
    }
    private static <T> T cast(Object x) { return (T)x; }
}

@Mixin(Feature.class)
abstract class FeatureMixins {
    @WrapMethod(method = "place(" +
        "Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;" +
        "Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;" +
        "Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z")
    private boolean place(FeatureConfiguration config, WorldGenLevel level, ChunkGenerator generator,
                          RandomSource random, BlockPos pos, Operation<Boolean> original) {
        return PlaceFeature.shouldProceed(cast(this), config, level, generator, random, pos) &&
            original.call(config, level, generator, random, pos);
    }
    private static <T> T cast(Object x) { return (T)x; }
}

@Mixin(Structure.class)
abstract class StructureMixins {
    @WrapMethod(method = "generate")
    private StructureStart generate(Holder<Structure> structure,
            ResourceKey<Level> levelKey, RegistryAccess registries,
            ChunkGenerator chunks, BiomeSource biomes, RandomState noise,
            StructureTemplateManager templates, long seed, ChunkPos pos, int references,
            LevelHeightAccessor level, Predicate<Holder<Biome>> biomeFilter,
            Operation<StructureStart> original) {
        return GenerateStructure.generate(cast(this),
            registries, chunks, biomes, noise, templates, seed, pos, references, level, biomeFilter);
    }
    private static <T> T cast(Object x) { return (T)x; }
}
