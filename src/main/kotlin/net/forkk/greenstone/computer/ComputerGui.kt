package net.forkk.greenstone.computer

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory

class ComputerScreen(container: ComputerGui, player: PlayerEntity) :
    CottonInventoryScreen<ComputerGui>(container, player)

class ComputerGui(syncId: Int, playerInventory: PlayerInventory, context: BlockContext) : CottonCraftingController(
    null,
    syncId,
    playerInventory,
    getBlockInventory(context),
    getBlockPropertyDelegate(context)
) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(256, 240)
        val textField = WTerminal()
        root.add(textField, 0, 0, 10, 2)
        root.validate(this)
    }
}
