package net.forkk.greenstone

import io.netty.buffer.Unpooled
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

var watcher: Job? = null

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

    // Server -> Client and Client -> Server
    // Sent server->client when user opens a file, and client->server when the user changes it.
    val PACKET_TERMINAL_EDIT_FILE = Identifier("greenstone", "packet_terminal_edit_file")

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
            val input = attachedData.readString(32767) // There is a *client-side-only* overload for calling this with no arguments; it always calls the main function  with this constant
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
        ServerSidePacketRegistry.INSTANCE.register(
            PACKET_TERMINAL_EDIT_FILE
        ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
            val contents = attachedData.readString(32767)
            packetContext.taskQueue.execute {
                ComputerBlockEntity.handleEdit(packetContext.player, contents)
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
    ClientSidePacketRegistry.INSTANCE.register(
        Greenstone.PACKET_TERMINAL_EDIT_FILE
    ) { packetContext: PacketContext, attachedData: PacketByteBuf ->
        val content = attachedData.readString()
        packetContext.taskQueue.execute {
            val screen = MinecraftClient.getInstance().currentScreen
            if (screen != null && screen is ComputerScreen) {
                startEditing(content)
            }
        }
    }
}

/**
 * Writes the given file contents to a temporary file and starts the user's text editor to edit the file.
 */
private fun startEditing(content: String) {
    val temp = File.createTempFile("editing", ".txt")
    temp.writeText(content)

    val path = Paths.get(temp.absolutePath)
    watcher?.cancel()
    watcher = GlobalScope.launch { watchFile(path) }

    openInEditor(temp)
}

private fun openInEditor(file: File) {
    // This is probably better but it seems to block.
    // Util.getOperatingSystem().open(file)
    val cmd = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        "start " + file.canonicalPath
    } else if (System.getProperty("os.name").toLowerCase().contains("macos")) {
        "open " + file.canonicalPath
    } else {
        "xdg-open " + file.canonicalPath
    }
    Runtime.getRuntime().exec(cmd)
}

private suspend fun watchFile(path: Path) {
    val watchService = withContext(Dispatchers.IO) {
        val w = FileSystems.getDefault()!!.newWatchService()!!
        path.parent.register(w, StandardWatchEventKinds.ENTRY_MODIFY)
        w
    }

    try {
        var key: WatchKey? = null
        while (true) {
            key = watchService.poll()
            if (key != null) {
                val events = key.pollEvents()
                for (ev in events) {
                    if (ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        val edited = ev.context() as Path
                        if (path.endsWith(edited)) {
                            sendEditedFile(path.toFile().readText())
                        }
                    }
                }
                key.reset()
            }
            try {
                delay(100)
            } catch (_: CancellationException) {
                break
            }
        }
    } finally {
        withContext(Dispatchers.IO) { watchService.close() }
    }
}

/**
 * Sends an edited file's contents to the server.
 */
private fun sendEditedFile(content: String) {
    val packetBuf = PacketByteBuf(Unpooled.buffer())
    packetBuf.writeString(content)
    ClientSidePacketRegistry.INSTANCE.sendToServer(Greenstone.PACKET_TERMINAL_EDIT_FILE, packetBuf)
}
