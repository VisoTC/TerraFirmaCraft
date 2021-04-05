/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.surfacebuilder;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.types.Rock;
import net.dries007.tfc.world.Codecs;
import net.dries007.tfc.world.chunkdata.ChunkData;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;

@SuppressWarnings("unused")
public class TFCSurfaceBuilders
{
    public static final DeferredRegister<SurfaceBuilder<?>> SURFACE_BUILDERS = DeferredRegister.create(ForgeRegistries.SURFACE_BUILDERS, MOD_ID);

    public static final RegistryObject<NormalSurfaceBuilder> NORMAL = register("normal", NormalSurfaceBuilder::new);
    public static final RegistryObject<BadlandsSurfaceBuilder> BADLANDS = register("badlands", BadlandsSurfaceBuilder::new);
    public static final RegistryObject<MountainSurfaceBuilder> MOUNTAINS = register("mountains", MountainSurfaceBuilder::new);
    public static final RegistryObject<ShoreSurfaceBuilder> SHORE = register("shore", ShoreSurfaceBuilder::new);
    public static final RegistryObject<FrozenUnderwaterSurfaceBuilder> ICEBERGS = register("icebergs", FrozenUnderwaterSurfaceBuilder::new);

    public static final RegistryObject<VolcanoesSurfaceBuilder> WITH_VOLCANOES = register("with_volcanoes", VolcanoesSurfaceBuilder::new, ParentedSurfaceBuilderConfig.CODEC);

    private static <S extends SurfaceBuilder<SurfaceBuilderConfig>> RegistryObject<S> register(String name, Function<Codec<SurfaceBuilderConfig>, S> factory)
    {
        return register(name, factory, Codecs.NOOP_SURFACE_BUILDER_CONFIG);
    }

    private static <C extends ISurfaceBuilderConfig, S extends SurfaceBuilder<C>> RegistryObject<S> register(String name, Function<Codec<C>, S> factory, Codec<C> codec)
    {
        return SURFACE_BUILDERS.register(name, () -> factory.apply(codec));
    }
}