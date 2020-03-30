package net.forkk.greenstone.computer

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget

class WTerminal() : WWidget() {
    /** Contains the contents of the screen. */
    private var screenBuf: String = ""

    /**
     * Prints the given string to the terminal screen.
     */
    fun termPrint(str: String) {
        screenBuf += str
    }

    /** Prints a line to the terminal screen. */
    fun termPrintln(str: String) = termPrint("$str\n")

    override fun canResize(): Boolean {
        return true
    }

    override fun paintBackground(x: Int, y: Int) {
        // Hey LibGUI documentation, maybe specify that your `color` field has an alpha value.
        ScreenDrawing.coloredRect(x, y, width, height, 0xFF000000.toInt())
        var texty = y + height
        for (line in screenBuf.lines().reversed()) {
            ScreenDrawing.drawString(line, x, texty, 0xFFFFFF)
            texty -= 12
        }
    }
}
