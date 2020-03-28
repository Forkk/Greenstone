package net.forkk.greenstone.grpl

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.Parser

object GRPLGrammar : Grammar<List<Statement>>() {
    private val floatLit by token("-?[0-9]+(\\.[0-9]+)")
    private val intLit by token("-?[0-9]+")
    private val strLit by token("\"[^\"]*\"")
    private val loadVar by token("<[^ ]+")
    private val storeVar by token(">[^ ]+")
    private val command by token("[a-zA-Z_-][a-zA-Z0-9_-]*")

    private val floatLitStmt by floatLit map { LitStmt(FloatVal(it.text.toDouble())) }
    private val intLitStmt by intLit map { LitStmt(IntVal(it.text.toInt())) }
    private val strLitStmt by strLit map { LitStmt(StringVal(it.text.substring(1, it.text.length - 1))) }

    private val loadStmt by loadVar map { LoadVarStmt(it.text.substring(1)) }
    private val storeStmt by storeVar map { StoreVarStmt(it.text.substring(1)) }

    private val commandStmt by command map { CommandStmt(it.text) }

    private val stmtParser by intLitStmt or floatLitStmt or strLitStmt or storeStmt or loadStmt or commandStmt
    private val stmtList by oneOrMore(stmtParser)

    override val rootParser: Parser<List<Statement>> by stmtList
}

fun parse(prgm: String): List<Statement> {
    return GRPLGrammar.parseToEnd(prgm)
}
