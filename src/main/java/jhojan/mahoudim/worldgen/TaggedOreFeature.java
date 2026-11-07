package jhojan.mahoudim.worldgen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class TaggedOreFeature extends Feature<NoneFeatureConfiguration> {
   private static final TagKey<Block> COMPATIBLE_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("mahoudim", "compatible_ores"));

   public TaggedOreFeature() {
      super(NoneFeatureConfiguration.CODEC);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      List<Block> ores = new ArrayList<>();

      for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(COMPATIBLE_ORES)) {
         Block block = (Block)holder.value();
         if (block != Blocks.AIR && block.defaultBlockState().isSolid()) {
            ores.add(block);
         }
      }

      if (ores.isEmpty()) {
         return false;
      } else {
         BlockPos origin = context.origin();
         WorldGenLevel level = context.level();
         RandomSource random = context.random();
         int placed = 0;
         int size = 6 + random.nextInt(5);

         for (int i = 0; i < size; i++) {
            BlockPos pos = origin.offset(random.nextInt(9) - 4, random.nextInt(9) - 4, random.nextInt(9) - 4);
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.TUFF)) {
               level.setBlock(pos, ores.get(random.nextInt(ores.size())).defaultBlockState(), 2);
               placed++;
            }
         }

         return placed > 0;
      }
   }
}
