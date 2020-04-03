package net.forkk.greenstone.computer

import com.github.h0tk3y.betterParse.parser.ParseException
import drawer.getFrom
import drawer.put
import io.netty.buffer.Unpooled
import java.util.function.Supplier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.forkk.greenstone.Greenstone
import net.forkk.greenstone.grpl.CommandGroup
import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.ContextSaveData
import net.forkk.greenstone.grpl.ExecError
import net.forkk.greenstone.grpl.GrplIO
import net.forkk.greenstone.grpl.GrplParser
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos

/**
 * Serializable data about a computer block entity that gets saved in the world.
 */
@Serializable
data class ComputerSaveData(
    val logs: String = "",
    val context: ContextSaveData = ContextSaveData()
)

class ComputerBlockEntity : BlockEntity(TYPE) {
    companion object {
        val TYPE: BlockEntityType<ComputerBlockEntity> = // Is this correct? idk, I copied it from Stockpile
            BlockEntityType.Builder.create(
                Supplier { ComputerBlockEntity() },
                ComputerBlock()
            ).build(null)

        /**
         * Keeps track of who has what terminal open.
         */
        private val openTerminalMap = hashMapOf<PlayerEntity, ComputerBlockEntity>()

        fun handleGuiOpen(player: PlayerEntity, pos: BlockPos) {
            val be = player.world.getBlockEntity(pos)
            if (be != null && be is ComputerBlockEntity) {
                if (openTerminalMap[player] != null) {
                    error("A terminal for ${player.name} is already open")
                }
                openTerminalMap[player] = be
                be.openPlayers += player
                be.sendLogs(player)
            } else {
                error("Player ${player.name} tried to register an open terminal with non-computer block at $pos")
            }
        }

        fun handleGuiClose(player: PlayerEntity) {
            val be = openTerminalMap[player] ?: return
            be.openPlayers.remove(player)
            openTerminalMap.remove(player)
        }

        fun handleInput(player: PlayerEntity, input: String) {
            val block = openTerminalMap[player]
            if (block == null) {
                error("Player sent input to a terminal, but they are not registered with one.")
            } else {
                block.onTerminalInput(input)
            }
        }

        fun handleInterrupt(player: PlayerEntity) {
            val block = openTerminalMap[player]
            if (block == null) {
                error("Player sent interrupt to a terminal, but they are not registered with one.")
            } else {
                block.interruptProgram()
            }
        }
    }

    private val extraCmds: List<CommandGroup> get() = listOf(ComputerIO(this).ioCommands())

    private val openPlayers: ArrayList<PlayerEntity> = arrayListOf()
    private var context: Context = Context(this.extraCmds)
    private var logs = ""
    /** The currently running job. Terminal input not allowed unless this is null */
    private var currentJob: Job? = null

    private val saveData: ComputerSaveData
        get() = ComputerSaveData(this.logs, this.context.saveData)

    override fun toTag(tag: CompoundTag): CompoundTag {
        ComputerSaveData.serializer().put(this.saveData, inTag = tag)
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        val data = ComputerSaveData.serializer().getFrom(tag)
        this.logs = data.logs
        this.context = data.context.toContext(this.extraCmds)
    }

    /**
     * Called when the player types input into this terminal.
     */
    private fun onTerminalInput(input: String) {
        if (currentJob?.isActive != true) {
            printToTerminal(">$input\n")
            startJob(input)
        }
    }

    /**
     * Stops the currently running program.
     */
    fun interruptProgram() {
        if (currentJob?.isActive == true) {
            currentJob?.cancel()
        }
    }

    /**
     * Starts executing a user's input asynchronously. Further input is blocked until this completes.
     */
    private fun startJob(input: String) {
        currentJob = GlobalScope.launch {
            try {
                context.exec(GrplParser.parse(input))
            } catch (e: ExecError) {
                world!!.server!!.execute { printToTerminal("${e.prettyMsg()}\n") }
            } catch (e: ParseException) {
                world!!.server!!.execute { printToTerminal("Parse Error: ${e.message}\n") }
            } catch (e: CancellationException) {
                world!!.server!!.execute { printToTerminal("Program Interrupted\n") }
            } finally {
                world!!.server!!.execute {
                    markDirty()
                    currentJob = null
                }
            }
        }
    }

    /**
     * Prints a string to the terminal and alerts all clients with the GUI open.
     */
    fun printToTerminal(str: String) {
        logs += str
        openPlayers.forEach { player ->
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeString(str)
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Greenstone.PACKET_TERMINAL_OUTPUT, passedData)
        }
    }

    /**
     * Sends the contents of the terminal screen to the given player.
     */
    private fun sendLogs(player: PlayerEntity) {
        if (world!!.isClient()) {
            error("Called sendLogs on clientside")
        }

        val passedData = PacketByteBuf(Unpooled.buffer())
        passedData.writeString(this.logs)
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Greenstone.PACKET_TERMINAL_CONTENTS, passedData)
    }

    /**
     * Clears terminal logs.
     *
     * Also sends a `TERMINAL_CONTENTS` packet to all players watching the logs, which clears their log windows.
     */
    fun clearTerminal() {
        this.logs = ""
        for (player in openPlayers) {
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeString(this.logs)
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Greenstone.PACKET_TERMINAL_CONTENTS, passedData)
        }
    }

    /**
     * Client-side only. Sends the terminal open packet to the server
     *
     * This will cause the server to start sending terminal output
     */
    fun openTerminal() {
        if (!world!!.isClient()) {
            error("Called openTerminal on serverside")
        }
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        packetBuf.writeBlockPos(this.pos)
        ClientSidePacketRegistry.INSTANCE.sendToServer(Greenstone.PACKET_TERMINAL_OPENED, packetBuf)
    }
}

class ComputerIO(private val block: ComputerBlockEntity) : GrplIO {
    override fun print(str: String) = block.world!!.server!!.execute {
        block.printToTerminal(str)
    }

    override fun clear() = block.world!!.server!!.execute {
        block.clearTerminal()
    }
}
