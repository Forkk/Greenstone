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
}
