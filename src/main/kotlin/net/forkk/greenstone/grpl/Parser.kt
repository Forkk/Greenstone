package net.forkk.greenstone.grpl

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser

object GRPLGrammar : Grammar<List<Statement>>() {
    private val trueLit by token("true")
    private val falseLit by token("false")
    private val floatLit by token("-?[0-9]+(\\.[0-9]+)")
    private val intLit by token("-?[0-9]+")
    private val strLit by token("\"[^\"]*\"")
    private val loadVar by token("<[^ ]+")
    private val storeVar by token(">[^ ]+")
    private val if_ by token("if")
    private val else_ by token("else")
    private val elif by token("elif")
    private val then by token("then")
    private val end by token("end")
    private val command by token("[a-zA-Z_-][a-zA-Z0-9_-]*")
    private val whitespace by token("[ \n\r]+", ignore = true)

    private val trueLitStmt by trueLit map { LitStmt(BoolVal(true)) }
    private val falseLitStmt by falseLit map { LitStmt(BoolVal(false)) }
    private val boolLitStmt by trueLitStmt or falseLitStmt
    private val floatLitStmt by floatLit map { LitStmt(FloatVal(it.text.toDouble())) }
    private val intLitStmt by intLit map { LitStmt(IntVal(it.text.toInt())) }
    private val strLitStmt by strLit map { LitStmt(StringVal(it.text.substring(1, it.text.length - 1))) }
    private val litStmt by boolLitStmt or intLitStmt or floatLitStmt or strLitStmt

    private val loadStmt by loadVar map { LoadVarStmt(it.text.substring(1)) }
    private val storeStmt by storeVar map { StoreVarStmt(it.text.substring(1)) }

    private val commandStmt by command map { CommandStmt(it.text) }

    private val body: Parser<List<Statement>> by parser(this::stmtList)
    private val ifCond by body * skip(then) * body map { (cond, body) ->
        IfCondition(cond, body)
    }
    private val ifStmt by skip(if_) * separatedTerms(ifCond, elif) *
            optional(skip(else_) * body) * skip(end) map { (conds, elsebody) ->
        IfStmt(conds, elsebody)
    }

    private val stmtParser by litStmt or storeStmt or loadStmt or commandStmt or ifStmt
    private val stmtList by separatedTerms(stmtParser, whitespace)

    override val rootParser: Parser<List<Statement>> by stmtList
}

fun parse(prgm: String): List<Statement> {
    return GRPLGrammar.parseToEnd(prgm)
}
