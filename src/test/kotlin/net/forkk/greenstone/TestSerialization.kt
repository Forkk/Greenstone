package net.forkk.greenstone

import drawer.getFrom
import drawer.put
import kotlin.test.Test
import kotlin.test.assertEquals
import net.forkk.greenstone.computer.ComputerSaveData
import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.ContextSaveData
import net.forkk.greenstone.grpl.GrplParser
import net.forkk.greenstone.grpl.IntVal
import net.minecraft.nbt.CompoundTag

class TestSerialization {
    @Test fun `test saving and loading context save data`() {
        val ctx = Context()
        ctx.exec(GrplParser.parse("42 >a 27 >b 13"))

        // Save the context to an NBT tag and then load it again.
        val tag = CompoundTag()
        ContextSaveData.serializer().put(ctx.saveData, inTag = tag)
        val load = ContextSaveData.serializer().getFrom(tag)

        // Test that our variables are still set and the stack is in the same state.
        val ctx2 = load.toContext()
        assertEquals(IntVal(42), ctx2.getVar("a"))
        assertEquals(IntVal(27), ctx2.getVar("b"))
        assertEquals(IntVal(13), ctx2.stack.peek())
    }

    @Test fun `test saving and loading computer save data`() {
        val ctx = Context()
        ctx.exec(GrplParser.parse("42 >a 27 >b 13"))
        val dat = ComputerSaveData("Test logs", ctx.saveData)

        // Save the data to an NBT tag and then load it again.
        val tag = CompoundTag()
        ComputerSaveData.serializer().put(dat, inTag = tag)
        val load = ComputerSaveData.serializer().getFrom(tag)

        assertEquals("Test logs", load.logs)

        // Test that our variables are still set and the stack is in the same state.
        val ctx2 = load.context.toContext()
        assertEquals(IntVal(42), ctx2.getVar("a"))
        assertEquals(IntVal(27), ctx2.getVar("b"))
        assertEquals(IntVal(13), ctx2.stack.peek())
    }
}
