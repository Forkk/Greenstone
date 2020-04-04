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

    @Test fun `test eq command with ints`() {
        val ctx1 = runProgram("2 2 eq")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("2 3 eq")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test eq command with floats`() {
        val ctx1 = runProgram("2.2 2.2 eq")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("2.2 3.2 eq")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test eq command with strings`() {
        val ctx1 = runProgram("\"foo\" \"foo\" eq")
        assertEquals(BoolVal(true), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("\"foo\" \"bar\" eq")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test eq command with different types`() {
        val ctx1 = runProgram("2.0 2 eq")
        assertEquals(BoolVal(false), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("2 \"2\" eq")
        assertEquals(BoolVal(false), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)

        val ctx3 = runProgram("true \"true\" eq")
        assertEquals(BoolVal(false), ctx3.stack.peek())
        assertEquals(1, ctx3.stack.size)
    }
}

class TestTypeCommands : InterpreterTest() {
    @Test fun `test int cast from int`() {
        val ctx1 = runProgram("42 int")
        assertEquals(IntVal(42), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test int cast from float`() {
        val ctx1 = runProgram("42.2 int")
        assertEquals(IntVal(42), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("42.0 int")
        assertEquals(IntVal(42), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test int cast from string`() {
        val ctx1 = runProgram("\"-42\" int")
        assertEquals(IntVal(-42), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("\"a\" int")
        assertEquals(NullVal, ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)

        val ctx3 = runProgram("\"42.2\" int")
        assertEquals(NullVal, ctx3.stack.peek())
        assertEquals(1, ctx3.stack.size)
    }

    @Test fun `test float cast from float`() {
        val ctx1 = runProgram("42.2 float")
        assertEquals(FloatVal(42.2), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test float cast from int`() {
        val ctx1 = runProgram("42 float")
        assertEquals(FloatVal(42.0), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test float cast from string`() {
        val ctx1 = runProgram("\"-42.2\" float")
        assertEquals(FloatVal(-42.2), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("\"a\" float")
        assertEquals(NullVal, ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test null to string`() {
        val ctx1 = runProgram("null str")
        assertEquals(StringVal("null"), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test int to string`() {
        val ctx1 = runProgram("42 str")
        assertEquals(StringVal("42"), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test float to string`() {
        val ctx1 = runProgram("42.2 str")
        assertEquals(StringVal("42.2"), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test bool to string`() {
        val ctx1 = runProgram("true str")
        assertEquals(StringVal("true"), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)

        val ctx2 = runProgram("false str")
        assertEquals(StringVal("false"), ctx2.stack.peek())
        assertEquals(1, ctx2.stack.size)
    }

    @Test fun `test string to string`() {
        val ctx1 = runProgram("\"foo\" str")
        assertEquals(StringVal("foo"), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }
}

class TestListcommands : InterpreterTest() {
    @Test fun `test newlist command`() {
        val ctx1 = runProgram("newlist")
        assertEquals(ListVal(listOf()), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test tolist command`() {
        val ctx1 = runProgram("newlist 1 2 3 tolist")
        assertEquals(ListVal(listOf(
            IntVal(1), IntVal(2), IntVal(3)
        )), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test tolist doesn't consume past list object`() {
        val ctx1 = runProgram("1 newlist 1 2 3 tolist")
        assertEquals(2, ctx1.stack.size)
        assertEquals(ListVal(listOf(
            IntVal(1), IntVal(2), IntVal(3)
        )), ctx1.stack.pop())
        assertEquals(IntVal(1), ctx1.stack.pop())
    }

    @Test fun `test list length`() {
        val ctx1 = runProgram("newlist 1 2 3 tolist len")
        assertEquals(IntVal(3), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
    }

    @Test fun `test list append`() {
        val ctx1 = runProgram("newlist 1 2 3 tolist 4 append")
        assertEquals(ListVal(listOf(
            IntVal(1), IntVal(2), IntVal(3), IntVal(4)
        )), ctx1.stack.peek())
        assertEquals(1, ctx1.stack.size)
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
