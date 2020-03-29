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

    @Test fun `test if statement`() {
        val ctx = runProgram("if true then 42 end")
        assertEquals(IntVal(42), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)

        val ctx2 = runProgram("if false then 42 end")
        assertEquals(0, ctx2.stack.size)
    }

    @Test fun `test if else statement`() {
        val ctx1 = runProgram("if true then 42 else 27 end")
        assertEquals(IntVal(42), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("if false then 42 else 27 end")
        assertEquals(IntVal(27), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test if elif else statement`() {
        val ctx1 = runProgram("if true then 42 elif false then 13 else 27 end")
        assertEquals(IntVal(42), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("if false then 42 elif true then 13 else 27 end")
        assertEquals(IntVal(13), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)

        val ctx3 = runProgram("if false then 42 elif false then 13 else 27 end")
        assertEquals(IntVal(27), ctx3.stack.peek())
        assertEquals(1, ctx3.stack.size)
    }

    @Test fun `test while statement`() {
        val ctx = runProgram("10 >a while <a 0 gt do <a <a 1 sub >a end")
        assertEquals(10, ctx.stack.size)
        for (i in 1..10) {
            assertEquals(IntVal(i), ctx.stack.pop())
        }
    }

    @Test fun `test function statements`() {
        val ctx = runProgram("fun 42 end @")
        assertEquals(1, ctx.stack.size)
        assertEquals(IntVal(42), ctx.stack.peek())
    }
}
