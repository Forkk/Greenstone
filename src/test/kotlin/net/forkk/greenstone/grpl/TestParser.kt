package net.forkk.greenstone.grpl

import kotlin.test.Test
import kotlin.test.assertEquals

class TestParser {
    private fun assertParsesTo(str: String, expect: List<Statement>) {
        val result = parse(str)
        assertEquals(expected = expect, actual = result)
    }

    @Test fun `test parsing basic commands`() {
        assertParsesTo("pop", listOf(CommandStmt("pop")))
        assertParsesTo("swap", listOf(CommandStmt("swap")))
    }

    @Test fun `test parsing store variable`() {
        assertParsesTo(">foo", listOf(StoreVarStmt("foo")))
    }

    @Test fun `test parsing load variable`() {
        assertParsesTo("<foo", listOf(LoadVarStmt("foo")))
    }

    @Test fun `test parsing boolean literals`() {
        assertParsesTo("true false", listOf(LitStmt(BoolVal(true)), LitStmt(BoolVal(false))))
    }

    @Test fun `test parsing int literal`() {
        assertParsesTo("42", listOf(LitStmt(IntVal(42))))
    }

    @Test fun `test parsing negative int literal`() {
        assertParsesTo("-42", listOf(LitStmt(IntVal(-42))))
    }

    @Test fun `test parsing float literal`() {
        assertParsesTo("42.0", listOf(LitStmt(FloatVal(42.0))))
    }

    @Test fun `test parsing negative float literal`() {
        assertParsesTo("-42.0", listOf(LitStmt(FloatVal(-42.0))))
    }

    @Test fun `test parsing a sequence of statements`() {
        assertParsesTo("42 >foo", listOf(LitStmt(IntVal(42)), StoreVarStmt("foo")))
    }

    @Test fun `test parsing a longer sequence of statements`() {
        assertParsesTo("42 >foo <foo 2 mul >foo",
            listOf(
                LitStmt(IntVal(42)), StoreVarStmt("foo"),
                LoadVarStmt("foo"), LitStmt(IntVal(2)), CommandStmt("mul"),
                StoreVarStmt("foo")
            ))
    }

    @Test fun `test parsing if statement`() {
        assertParsesTo("if true then 42 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true))), listOf(LitStmt(IntVal(42))))
            ), null)
        ))
    }

    @Test fun `test parsing if else statement`() {
        assertParsesTo("if true then 42 else 27 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true))), listOf(LitStmt(IntVal(42))))
            ), listOf(LitStmt(IntVal(27))))
        ))
    }

    @Test fun `test parsing if elif else statement`() {
        assertParsesTo("if true then 42 elif false then 13 else 27 end", listOf(
            IfStmt(listOf(
                IfCondition(listOf(LitStmt(BoolVal(true))), listOf(LitStmt(IntVal(42)))),
                IfCondition(listOf(LitStmt(BoolVal(false))), listOf(LitStmt(IntVal(13))))
            ), listOf(LitStmt(IntVal(27))))
        ))
    }

    @Test fun `test parsing while statement`() {
        assertParsesTo("while true do 42 end", listOf(
            WhileStmt(listOf(LitStmt(BoolVal(true))), listOf(LitStmt(IntVal(42))))
        ))
    }

    @Test fun `test parsing anonymous function call`() {
        assertParsesTo("@", listOf(CallStmt("")))
    }

    @Test fun `test parsing named function call`() {
        assertParsesTo("@foo", listOf(CallStmt("foo")))
    }

    @Test fun `test parsing named function declaration`() {
        assertParsesTo("fun@foo 27 42 add end", listOf(
            FunStmt("foo", listOf(
                LitStmt(IntVal(27)),
                LitStmt(IntVal(42)),
                CommandStmt("add")
            ))
        ))
    }

    @Test fun `test parsing function literal`() {
        assertParsesTo("fun 27 42 add end", listOf(
            FunStmt("", listOf(
                LitStmt(IntVal(27)),
                LitStmt(IntVal(42)),
                CommandStmt("add")
            ))
        ))
    }
}
