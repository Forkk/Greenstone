package net.forkk.greenstone

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.forkk.greenstone.computer.ComputerBlock
import net.forkk.greenstone.computer.ComputerBlockEntity
import net.forkk.greenstone.computer.ComputerScreen
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome

@Suppress("unused")
object Greenstone : ModInitializer {
    private val GREENSTONE_DUST = Item(Item.Settings().group(ItemGroup.REDSTONE))
    private val GREENSTONE_ORE = GreenstoneOre()
    private val GREENSTONE_BLOCK = Block(FabricBlockSettings.copy(Blocks.REDSTONE_BLOCK).build())

    private val COMPUTER = ComputerBlock()

    // // Client -> server packets
    // Packet for opening and closing the terminal
    val PACKET_TERMINAL_OPENED = Identifier("greenstone", "packet_terminal_opened")
    val PACKET_TERMINAL_CLOSED = Identifier("greenstone", "packet_terminal_closed")
    // Packet for when the user inputs something
    val PACKET_TERMINAL_INPUT = Identifier("greenstone", "packet_terminal_input")
    // Packet for when the user interrupts the program with Ctrl+C.
    val PACKET_TERMINAL_INTERRUPT = Identifier("greenstone", "packet_terminal_interrupt")

    // // Server -> Client packets
    // Packet for sending new terminal output
    val PACKET_TERMINAL_OUTPUT = Identifier("greenstone", "packet_terminal_output")
    // Packet for sending screen contents when terminal is opened
    val PACKET_TERMINAL_CONTENTS = Identifier("greenstone", "packet_terminal_contents")

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
        // Loop over existing biomes
        Registry.BIOME.forEach { GREENSTONE_ORE.handleBiome(it) }

        // Listen for other biomes being registered
        RegistryEntryAddedCallback.event(Registry.BIOME)
            .register(RegistryEntryAddedCallback { _: Int, _: Identifier, biome: Biome ->
                GREENSTONE_ORE.handleBiome(biome)
            })

        Registry.register(Registry.BLOCK, Identifier("greenstone", "computer"), COMPUTER)
        Registry.register(
            Registry.ITEM,
            Identifier("greenstone", "computer"),
            BlockItem(COMPUTER, Item.Settings().group(ItemGroup.REDSTONE))
        )

        Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier("greenstone", "computer"), ComputerBlockEntity.TYPE)

        ServerSidePacketRegistry.INSTANCE.register(
            PACKET_TERMINAL_INPUT
        ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
            val input = attachedData.readString()
            packetContext.taskQueue.execute {
                ComputerBlockEntity.handleInput(packetContext.player, input)
            }
        }
        ServerSidePacketRegistry.INSTANCE.register(
            PACKET_TERMINAL_OPENED
        ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
            val pos = attachedData.readBlockPos()
            packetContext.taskQueue.execute {
                ComputerBlockEntity.handleGuiOpen(packetContext.player, pos)
            }
        }
        ServerSidePacketRegistry.INSTANCE.register(
            PACKET_TERMINAL_CLOSED
        ) { packetContext: PacketContext, _: PacketByteBuf ->
            packetContext.taskQueue.execute {
                ComputerBlockEntity.handleGuiClose(packetContext.player)
            }
        }
        ServerSidePacketRegistry.INSTANCE.register(
            PACKET_TERMINAL_INTERRUPT
        ) { packetContext: PacketContext, _: PacketByteBuf ->
            packetContext.taskQueue.execute {
                ComputerBlockEntity.handleInterrupt(packetContext.player)
            }
        }
    }
}

@Suppress("unused")
fun clientInit() {
    ClientSidePacketRegistry.INSTANCE.register(
        Greenstone.PACKET_TERMINAL_OUTPUT
    ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val str = attachedData.readString()
        packetContext.taskQueue.execute {
            val screen = MinecraftClient.getInstance().currentScreen
            if (screen != null && screen is ComputerScreen) {
                screen.gui.onPrintReceived(str)
            }
        }
    }
    ClientSidePacketRegistry.INSTANCE.register(
        Greenstone.PACKET_TERMINAL_CONTENTS
    ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val str = attachedData.readString()
        packetContext.taskQueue.execute {
            val screen = MinecraftClient.getInstance().currentScreen
            if (screen != null && screen is ComputerScreen) {
                screen.gui.onContentsReceived(str)
            }
        }
    }
}
