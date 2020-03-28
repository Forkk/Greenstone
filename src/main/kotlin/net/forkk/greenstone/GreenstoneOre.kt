package net.forkk.greenstone

import net.minecraft.block.OreBlock
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.decorator.RangeDecoratorConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig

class GreenstoneOre(private val settings: Settings) : OreBlock(settings) {
    fun handleBiome(biome: Biome) {
        if (biome.category !== Biome.Category.NETHER && biome.category !== Biome.Category.THEEND) {
            biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE.configure(
                    OreFeatureConfig(
                        OreFeatureConfig.Target.NATURAL_STONE,
                        this.defaultState,
                        8 // Ore vein size
                    )
                ).createDecoratedFeature(
                    Decorator.COUNT_RANGE.configure(
                        RangeDecoratorConfig(
                            8, // Number of veins per chunk
                            0, // Bottom Offset
                            0, // Min y level
                            16 // Max y level
                        )
                    )
                )
            )
        }
    }
}
