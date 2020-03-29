package net.forkk.greenstone.computer

import io.github.cottonmc.cotton.gui.widget.WTextField
import org.lwjgl.glfw.GLFW

class WTerminal() : WTextField() {
    init {
        requestFocus()
    }

    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        super.onKeyPressed(ch, key, modifiers)
        if (modifiers == 0 && ch == GLFW.GLFW_KEY_ENTER) {
            println("Content of TextField: " + this.text)
        }
    }
}
