package net.forkk.greenstone.computer

import com.github.h0tk3y.betterParse.parser.ParseException
import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WTextField
import net.forkk.greenstone.grpl.Command
import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.ExecError
import net.forkk.greenstone.grpl.ValueType
import net.forkk.greenstone.grpl.parse
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import org.lwjgl.glfw.GLFW

class ComputerScreen(container: ComputerGui, player: PlayerEntity) :
    CottonInventoryScreen<ComputerGui>(container, player)

class ComputerGui(syncId: Int, playerInventory: PlayerInventory, context: BlockContext) : CottonCraftingController(
    null,
    syncId,
    playerInventory,
    getBlockInventory(context),
    getBlockPropertyDelegate(context)
) {
    private val termWidget = WTerminal()
    private val interpreter = Context(arrayOf(TermPrintCommand(termWidget)))
    private val textWidget = WLineEdit { input ->
        if (input.isNotBlank()) {
            termWidget.termPrintln(">$input")
            try {
                interpreter.exec(parse(input))
            } catch (e: ExecError) {
                termWidget.termPrintln("Error: ${e.message}")
            } catch (e: ParseException) {
                termWidget.termPrintln("Parse Error: ${e.message}")
            }
        }
    }

    init {
        val root = WPlainPanel()
        setRootPanel(root)
        root.setSize(256, 240)
        root.add(termWidget, 0, 0, 256, 220)
        root.add(textWidget, 0, 220, 256, 20)
        root.validate(this)
        termWidget.requestFocus()
    }
}

class WLineEdit(private val enterCallback: (String) -> Unit) : WTextField() {
    init {
        maxLength = 120
    }
    override fun getMaxLength(): Int = 120
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        super.onKeyPressed(ch, key, modifiers)
        if (modifiers == 0 && ch == GLFW.GLFW_KEY_ENTER) {
            this.enterCallback(this.text)
            this.text = ""
        }
    }
}

/** Print command for WTerminal */
class TermPrintCommand(private val term: WTerminal) : Command("print") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        val str = if (v.isType(ValueType.STRING)) {
            v.asString()
        } else {
            v.toString()
        }
        term.termPrintln(str)
    }

    override val help: String
        get() = "Pops a value off the top of the stack and prints it to the terminal."
}
