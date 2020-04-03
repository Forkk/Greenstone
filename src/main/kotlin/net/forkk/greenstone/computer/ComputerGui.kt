package net.forkk.greenstone.computer

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WClippedPanel
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WScrollBar
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.forkk.greenstone.Greenstone
import net.minecraft.util.PacketByteBuf
import org.lwjgl.glfw.GLFW

class ComputerScreen(val gui: ComputerGui = ComputerGui()) : CottonClientScreen(gui) {
    override fun onClose() {
        super.onClose()
        gui.onClose()
    }
}

class ComputerGui() : LightweightGuiDescription() {
    private val scrollWidget = object : WScrollBar(Axis.VERTICAL) {
        init { this.setWindow(220) }
        // Why is this false by default? Who wants a square, non-resizable scrollbar?
        override fun canResize(): Boolean = true
    }

    private val termWidget = WTerminal("Connecting...", scrollWidget)
    private val textWidget = WLineEdit({ this.interrupt() }, { input ->
        if (input.isNotBlank()) {
            sendInput(input)
        }
    })

    init {
        val root = WPlainPanel()
        setRootPanel(root)
        root.setSize(256, 240)

        // Is this how you're meant to add children to a clipped panel? Probably not, but there seems to be no other way
        val termPanel = object : WClippedPanel() {
            init { children += termWidget }
        }
        termWidget.setSize(256, 220)
        root.add(termPanel, 0, 0, 256, 220)

        root.add(textWidget, 0, 220, 256, 20)
        root.add(scrollWidget, 248, 0, 8, 220)

        root.validate(this)
    }

    /**
     * Sends the given input to the server.
     */
    fun sendInput(str: String) {
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        packetBuf.writeString(str)
        ClientSidePacketRegistry.INSTANCE.sendToServer(Greenstone.PACKET_TERMINAL_INPUT, packetBuf)
    }

    /**
     * Sends the server an interrupt signal.
     */
    fun interrupt() {
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        ClientSidePacketRegistry.INSTANCE.sendToServer(Greenstone.PACKET_TERMINAL_INTERRUPT, packetBuf)
    }

    /**
     * Tells the server the GUI is closed.
     */
    fun onClose() {
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        ClientSidePacketRegistry.INSTANCE.sendToServer(Greenstone.PACKET_TERMINAL_CLOSED, packetBuf)
    }

    /**
     * Called by a packet handler when the server notifies the client of new output.
     */
    fun onPrintReceived(str: String) {
        termWidget.termPrint(str)
    }

    /**
     * Called by a packet handler when the server notifies the client of the initial screen contents.
     */
    fun onContentsReceived(str: String) {
        termWidget.setContents(str)
    }
}

class WLineEdit(
    private val interruptCallback: () -> Unit,
    private val enterCallback: (String) -> Unit
) : WTextField() {
    init {
        maxLength = 120
    }
    override fun getMaxLength(): Int = 120
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        super.onKeyPressed(ch, key, modifiers)
        if (modifiers == 0 && ch == GLFW.GLFW_KEY_ENTER) {
            this.enterCallback(this.text)
            this.text = ""
        } else if (modifiers == GLFW.GLFW_MOD_CONTROL && ch == GLFW.GLFW_KEY_C) {
            this.interruptCallback()
        }
    }
}
