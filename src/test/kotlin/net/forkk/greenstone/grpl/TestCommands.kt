package net.forkk.greenstone.grpl

import kotlin.test.Test
import kotlin.test.assertEquals

class TestCommands : InterpreterTest() {
    @Test fun `test pop command`() {
        val ctx = runProgram("42 pop")
        assertEquals(0, ctx.stack.size)
    }
    @Test fun `test dup command`() {
        val ctx = runProgram("42 dup")
        assertEquals(2, ctx.stack.size)
        assertEquals(IntVal(42), ctx.stack.pop())
        assertEquals(IntVal(42), ctx.stack.pop())
    }
    @Test fun `test swap command`() {
        val ctx = runProgram("42 27 swap")
        assertEquals(2, ctx.stack.size)
        assertEquals(IntVal(42), ctx.stack.pop())
        assertEquals(IntVal(27), ctx.stack.pop())
    }
}

class TestBoolCommands : InterpreterTest() {
    @Test fun `test not command`() {
        val ctx1 = runProgram("true not")
        assertEquals(BoolVal(false), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("false not")
        assertEquals(BoolVal(true), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test and command`() {
        val ctx1 = runProgram("true false and")
        assertEquals(BoolVal(false), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("true true and")
        assertEquals(BoolVal(true), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test or command`() {
        val ctx1 = runProgram("true false or")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("false false or")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }
}

class TestMathCommands : InterpreterTest() {
    @Test fun `test gt command`() {
        val ctx1 = runProgram("3 2 gt")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("2 3 gt")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test lt command`() {
        val ctx1 = runProgram("2 3 lt")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("3 2 lt")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test add command`() {
        val ctx = runProgram("42 27 add")
        assertEquals(IntVal(69), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }

    @Test fun `test sub command`() {
        val ctx = runProgram("42 27 sub")
        assertEquals(IntVal(42 - 27), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }

    @Test fun `test mul command`() {
        val ctx = runProgram("42 27 mul")
        assertEquals(IntVal(42 * 27), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }

    @Test fun `test integer div command on ints`() {
        val ctx = runProgram("42 27 idiv")
        assertEquals(IntVal(42 / 27), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
    @Test fun `test integer div command on floats`() {
        val ctx = runProgram("42.0 27.0 idiv")
        assertEquals(IntVal(42 / 27), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
    @Test fun `test integer div command on mixed`() {
        val ctx = runProgram("42.0 27 idiv")
        assertEquals(IntVal(42 / 27), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }

    @Test fun `test float div command on ints`() {
        val ctx = runProgram("42 27 div")
        assertEquals(FloatVal(42.0 / 27.0), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
    @Test fun `test float div command on floats`() {
        val ctx = runProgram("42.0 27.0 div")
        assertEquals(FloatVal(42.0 / 27.0), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
    @Test fun `test float div command on mixed`() {
        val ctx = runProgram("42.0 27 div")
        assertEquals(FloatVal(42.0 / 27.0), ctx.stack.peek())
        assertEquals(1, ctx.stack.size)
    }
}
