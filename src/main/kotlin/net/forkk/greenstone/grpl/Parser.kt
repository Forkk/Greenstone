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
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.Parser

private class GRPLGrammar(private val input: String) : Grammar<List<Statement>>() {
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
    private val while_ by token("while")
    private val namedFun by token("fun@[^ ]*")
    private val fun_ by token("fun")
    private val do_ by token("do")
    private val end by token("end")
    private val call by token("@[^ ]*")
    private val command by token("[a-zA-Z_-][a-zA-Z0-9_-]*")
    private val whitespace by token("[ \n\r]+", ignore = true)

    private val trueLitStmt by trueLit map { LitStmt(BoolVal(true), sourceLoc(it)) }
    private val falseLitStmt by falseLit map { LitStmt(BoolVal(false), sourceLoc(it)) }
    private val boolLitStmt by trueLitStmt or falseLitStmt
    private val floatLitStmt by floatLit map { LitStmt(FloatVal(it.text.toDouble()), sourceLoc(it)) }
    private val intLitStmt by intLit map { LitStmt(IntVal(it.text.toInt()), sourceLoc(it)) }
    private val strLitStmt by strLit map { LitStmt(StringVal(it.text.substring(1, it.text.length - 1)), sourceLoc(it)) }
    private val litStmt by boolLitStmt or intLitStmt or floatLitStmt or strLitStmt

    private val loadStmt by loadVar map { LoadVarStmt(it.text.substring(1), sourceLoc(it)) }
    private val storeStmt by storeVar map { StoreVarStmt(it.text.substring(1), sourceLoc(it)) }

    private val commandStmt by command map { CommandStmt(it.text, sourceLoc(it)) }

    private val body: Parser<List<Statement>> by parser(this::stmtList)
    private val ifCond by body * skip(then) * body map { (cond, body) ->
        IfCondition(cond, body)
    }
    private val ifStmt by skip(if_) * separatedTerms(ifCond, elif) *
            optional(skip(else_) * body) * skip(end) map { (conds, elsebody) ->
        IfStmt(conds, elsebody)
    }
    private val whileStmt by skip(while_) * body * skip(do_) * body * skip(end) map { (cond, body) ->
        WhileStmt(cond, body)
    }
    private val funLitStmt by skip(fun_) * body * skip(end) map { FunStmt("", it) }
    private val namedFunStmt by namedFun * body * skip(end) map { (name, body) ->
        // Trim the "fun@" off the `name` match.
        FunStmt(name.text.substring(4), body)
    }
    private val funStmt by namedFunStmt or funLitStmt
    private val callStmt by call map { CallStmt(it.text.substring(1), sourceLoc(it)) }
    private val complexStmt by ifStmt or whileStmt or funStmt or callStmt

    private val stmtParser by litStmt or storeStmt or loadStmt or commandStmt or complexStmt
    private val stmtList by separatedTerms(stmtParser, whitespace)

    override val rootParser: Parser<List<Statement>> by stmtList

    private fun sourceLoc(match: TokenMatch): SourceLocation = SourceLocation.fromTokenMatch(input, match)
}

object GrplParser {
    fun parse(prgm: String): List<Statement> {
        return GRPLGrammar(prgm).parseToEnd(prgm)
    }
}
