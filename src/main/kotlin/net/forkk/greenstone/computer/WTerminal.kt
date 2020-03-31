package net.forkk.greenstone.computer

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.MinecraftClient

private const val LINE_HEIGHT = 12

class WTerminal(initMsg: String) : WWidget() {
    /** Contains the contents of the screen. */
    private var screenBuf: String = initMsg

    private val padding = 2 // 4 px padding each side
    private val innerWidth: Int get() = width - padding * 2

    /**
     * Prints the given string to the terminal screen.
     */
    fun termPrint(str: String) {
        screenBuf += str
    }

    /**
     * Clears the screen and sets the contents of the terminal to `str`.
     */
    fun setContents(str: String) {
        screenBuf = str
    }

    override fun canResize(): Boolean {
        return true
    }

    override fun paintBackground(x: Int, y: Int) {
        // Hey LibGUI documentation, maybe specify that your `color` field has an alpha value.
        ScreenDrawing.coloredRect(x, y, width, height, 0xFF000000.toInt())
        var texty = y + height
        for (line in screenBuf.lines().reversed()) {
            texty = paintLine(x, texty, line)
        }
    }

    /**
     * Paints a line of output on the terminal, with wrapping.
     *
     * Lines are painted bottom to top, and this returns the y position of the top of the line.
     */
    private fun paintLine(outerx: Int, starty: Int, str: String): Int {
        val x = outerx + padding
        var y = starty
        val lines = MinecraftClient.getInstance().textRenderer.wrapStringToWidthAsList(str, innerWidth)
        for (line in lines.reversed()) {
            ScreenDrawing.drawString(line, x, y, 0xFFFFFF)
            y -= LINE_HEIGHT
        }
        return y
    }
}
