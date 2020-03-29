package net.forkk.greenstone.grpl

/**
 * Represents a statement that can be executed.
 */
sealed class Statement {
    /**
     * Executes this statement in the given context.
     */
    abstract fun exec(ctx: Context)
}

/** Pushes a literal value */
data class LitStmt(val value: Value) : Statement() {
    override fun exec(ctx: Context) { ctx.stack.push(value) }
}

/** Pushes the value of a variable on the stack */
data class LoadVarStmt(val name: String) : Statement() {
    override fun exec(ctx: Context) {
        val value = ctx.getVar(name)
        ctx.stack.push(value)
    }
}

/** Sets the value of a variable to the value on top of the stack. */
data class StoreVarStmt(val name: String) : Statement() {
    override fun exec(ctx: Context) {
        val value = ctx.stack.pop()
        ctx.setVar(name, value)
    }
}

/** Executes a built-in command */
data class CommandStmt(val cmdname: String) : Statement() {
    override fun exec(ctx: Context) {
        val cmd = ctx.commands.get(cmdname)
        if (cmd == null) {
            throw UnknownCommandError(cmdname)
        } else {
            cmd.exec(ctx)
        }
    }
}

/**
 * An if or elif part of an if statement. The actual `IfStmt` contains a list of these
 * and an `else` block.
 */

data class IfCondition(val cond: List<Statement>, val body: List<Statement>) {
    /**
     * Runs the `cond` statements and, if true is left on the stack, runs `body` and returns true.
     */
    fun exec(ctx: Context): Boolean {
        ctx.exec(cond)
        return if (ctx.stack.pop().asBoolOrErr()) {
            ctx.exec(body)
            true
        } else { false }
    }
}

/**
 * The actual conditional statement. Contains one or more `IfCondition`s. The first represents the if condition, and
 * the rest represent elif conditions. There is also an optional body for an else clause.
 */
data class IfStmt(val conds: List<IfCondition>, val else_: List<Statement>?) : Statement() {
    override fun exec(ctx: Context) {
        for (cond in conds) {
            if (cond.exec(ctx)) {
                return
            }
        }
        if (else_ != null && else_.isNotEmpty()) {
            ctx.exec(else_)
        }
    }
}

/**
 * A while loop statement. Executes `body` repeatedly as long as executing `cond` continues to push true on the stack.
 */
data class WhileStmt(val cond: List<Statement>, val body: List<Statement>) : Statement() {
    override fun exec(ctx: net.forkk.greenstone.grpl.Context) {
        while (true) {
            ctx.exec(cond)
            if (ctx.stack.pop().asBoolOrErr()) {
                ctx.exec(body)
            } else { break }
        }
    }
}
