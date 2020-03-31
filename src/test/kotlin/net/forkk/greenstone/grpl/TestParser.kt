package net.forkk.greenstone.grpl

import kotlin.test.Test
import kotlin.test.assertEquals

class TestParser {
    private fun assertParsesTo(str: String, expect: List<Statement>) {
        val result = GrplParser.parse(str)
        assertEquals(expected = expect, actual = result)
    }

    private fun sourceLoc(): SourceLocation = TestSourceLocation()

    @Test fun `test parsing basic commands`() {
        assertParsesTo("pop", listOf(CommandStmt("pop", sourceLoc())))
        assertParsesTo("swap", listOf(CommandStmt("swap", sourceLoc())))
    }

    @Test fun `test parsing store variable`() {
        assertParsesTo(">foo", listOf(StoreVarStmt("foo", sourceLoc())))
    }

    @Test fun `test parsing load variable`() {
        assertParsesTo("<foo", listOf(LoadVarStmt("foo", sourceLoc())))
    }

    @Test fun `test parsing boolean literals`() {
        assertParsesTo("true false", listOf(
            LitStmt(BoolVal(true), sourceLoc()),
            LitStmt(BoolVal(false), sourceLoc())
        ))
    }

    @Test fun `test parsing int literal`() {
        assertParsesTo("42", listOf(LitStmt(IntVal(42), sourceLoc())))
    }

    @Test fun `test parsing negative int literal`() {
        assertParsesTo("-42", listOf(LitStmt(IntVal(-42), sourceLoc())))
    }

    @Test fun `test parsing float literal`() {
        assertParsesTo("42.0", listOf(LitStmt(FloatVal(42.0), sourceLoc())))
    }

    @Test fun `test parsing negative float literal`() {
        assertParsesTo("-42.0", listOf(LitStmt(FloatVal(-42.0), sourceLoc())))
    }

    @Test fun `test parsing a sequence of statements`() {
        assertParsesTo("42 >foo", listOf(LitStmt(IntVal(42), sourceLoc()), StoreVarStmt("foo", sourceLoc())))
    }

    @Test fun `test parsing a longer sequence of statements`() {
        assertParsesTo("42 >foo <foo 2 mul >foo",
            listOf(
                LitStmt(IntVal(42), sourceLoc()), StoreVarStmt("foo", sourceLoc()),
                LoadVarStmt("foo", sourceLoc()), LitStmt(IntVal(2), sourceLoc()),
                CommandStmt("mul", sourceLoc()),
                StoreVarStmt("foo", sourceLoc())
            ))
    }

    @Test fun `test parsing if statement`() {
        assertParsesTo("if true then 42 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true), sourceLoc())), listOf(LitStmt(IntVal(42), sourceLoc())))
            ), null)
        ))
    }

    @Test fun `test parsing if else statement`() {
        assertParsesTo("if true then 42 else 27 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true), sourceLoc())), listOf(LitStmt(IntVal(42), sourceLoc())))
            ), listOf(LitStmt(IntVal(27), sourceLoc())))
        ))
    }

    @Test fun `test parsing if elif else statement`() {
        assertParsesTo("if true then 42 elif false then 13 else 27 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true), sourceLoc())), listOf(LitStmt(IntVal(42), sourceLoc()))),
                IfCondition(listOf(LitStmt(BoolVal(false), sourceLoc())), listOf(LitStmt(IntVal(13), sourceLoc())))
            ), listOf(LitStmt(IntVal(27), sourceLoc())))
        ))
    }

    @Test fun `test parsing while statement`() {
        assertParsesTo("while true do 42 end", listOf(
            WhileStmt(listOf(LitStmt(BoolVal(true), sourceLoc())), listOf(LitStmt(IntVal(42), sourceLoc())))
        ))
    }

    @Test fun `test parsing anonymous function call`() {
        assertParsesTo("@", listOf(CallStmt("", sourceLoc())))
    }

    @Test fun `test parsing named function call`() {
        assertParsesTo("@foo", listOf(CallStmt("foo", sourceLoc())))
    }

    @Test fun `test parsing named function declaration`() {
        assertParsesTo("fun@foo 27 42 add end", listOf(
            FunStmt("foo", listOf(
                LitStmt(IntVal(27), sourceLoc()),
                LitStmt(IntVal(42), sourceLoc()),
                CommandStmt("add", sourceLoc())
            ))
        ))
    }

    @Test fun `test parsing function literal`() {
        assertParsesTo("fun 27 42 add end", listOf(
            FunStmt("", listOf(
                LitStmt(IntVal(27), sourceLoc()),
                LitStmt(IntVal(42), sourceLoc()),
                CommandStmt("add", sourceLoc())
            ))
        ))
    }
}
