package net.forkk.greenstone.grpl

import kotlin.test.Test
import kotlin.test.assertEquals

open class InterpreterTest {
    protected fun runProgram(prgmstr: String): Context {
        val prgm = parse(prgmstr)
        val ctx = Context()
        ctx.exec(prgm)
        return ctx
    }
}

class TestStatements : InterpreterTest() {
    @Test fun `test push int on the stack`() {
        val ctx = runProgram("42")
        assertEquals(IntVal(42), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }

    @Test fun `test stack order`() {
        val ctx = runProgram("42 27")
        assertEquals(2, ctx.stack.size)
        assertEquals(IntVal(27), ctx.stack.pop())
        assertEquals(IntVal(42), ctx.stack.pop())
    }

    @Test fun `test store variable`() {
        val ctx = runProgram("42 >foo")
        assertEquals(IntVal(42), ctx.getVar("foo"))
        assertEquals(0, ctx.stack.size)
    }

    @Test fun `test load variable`() {
        val ctx = runProgram("42 >foo <foo")
        assertEquals(IntVal(42), ctx.getVar("foo"))
        assertEquals(IntVal(42), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
}
