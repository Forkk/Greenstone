package net.forkk.greenstone

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome

@Suppress("unused")
object Greenstone : ModInitializer {
    override fun onInitialize() {
        Registry.register(Registry.ITEM, Identifier("greenstone", "greenstone_dust"), GREENSTONE_DUST)

        Registry.register(Registry.BLOCK, Identifier("greenstone", "greenstone_ore"), GREENSTONE_ORE)
        Registry.register(
            Registry.ITEM, Identifier("greenstone", "greenstone_ore"),
            BlockItem(GREENSTONE_ORE, Item.Settings().group(ItemGroup.BUILDING_BLOCKS))
        )
        Registry.register(Registry.BLOCK, Identifier("greenstone", "greenstone_block"), GREENSTONE_BLOCK)
        Registry.register(
            Registry.ITEM, Identifier("greenstone", "greenstone_block"), BlockItem(
                GREENSTONE_BLOCK, Item.Settings().group(
                    ItemGroup.BUILDING_BLOCKS
                )
            )
        )

        Registry.register(Registry.BLOCK, Identifier("greenstone", "computer"), COMPUTER)
        Registry.register(
            Registry.ITEM,
            Identifier("greenstone", "computer"),
            BlockItem(COMPUTER, Item.Settings().group(ItemGroup.REDSTONE))
        )

        // Loop over existing biomes
        Registry.BIOME.forEach { GREENSTONE_ORE.handleBiome(it) }

        // Listen for other biomes being registered
        RegistryEntryAddedCallback.event(Registry.BIOME)
            .register(RegistryEntryAddedCallback { _: Int, _: Identifier, biome: Biome ->
                GREENSTONE_ORE.handleBiome(biome)
            })
    }

    private val GREENSTONE_DUST = Item(Item.Settings().group(ItemGroup.REDSTONE))
    private val GREENSTONE_ORE = GreenstoneOre(
        FabricBlockSettings.of(Material.STONE)
            .strength(3.0f, 3.0f).build()
    )
    private val GREENSTONE_BLOCK = Block(FabricBlockSettings.copy(Blocks.REDSTONE_BLOCK).build())

    private val COMPUTER = ComputerBlock(FabricBlockSettings.copy(Blocks.IRON_BLOCK).build())
}
