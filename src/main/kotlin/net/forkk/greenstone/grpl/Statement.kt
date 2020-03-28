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
