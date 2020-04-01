package net.forkk.greenstone.grpl

/**
 * This interface is used to specify how a set of IO commands should actually input or output their data.
 *
 * Once an IO interface has been defined, a set of IO commands that use the interface can be created with `ioCommands`.
 */
interface GrplIO {
    /** Prints the given string to the terminal. */
    fun print(str: String)

    /** Prints the given string followed by a new line */
    fun println(str: String) = this.print("$str\n")

    /** Clears the terminal screen. */
    fun clear()

    /**
     * Generates a group of IO commands which will use this interface.
     */
    fun ioCommands(): CommandGroup = CommandGroup("io",
        "Commands for terminal input/output",
        PrintCommand(this),
        ClearCommand(this),
        TraceCommand(this),
        HelpCommand(this)
    )
}

class PrintCommand(private val io: GrplIO) : Command("print") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        val str = if (v.isType(ValueType.STRING)) {
            v.asString()
        } else {
            v.toString()
        }
        io.println(str)
    }

    override val help: String
        get() = "Pops a value off the top of the stack and prints it to the terminal."
}

class ClearCommand(private val io: GrplIO) : Command("clear") {
    override fun exec(ctx: Context) {
        io.clear()
    }

    override val help: String
        get() = "Pops a value off the top of the stack and prints it to the terminal."
}

class TraceCommand(private val io: GrplIO) : Command("trace") {
    override fun exec(ctx: Context) {
        io.println(ctx.stack.list.joinToString { it.toString() })
    }

    override val help: String
        get() = "Prints the current value stack to the terminal in order from bottom to top, without removing any items. " +
                "Use for debugging."
}

class HelpCommand(private val io: GrplIO) : Command("help") {
    override fun exec(ctx: Context) {
        if (ctx.stack.size > 0) {
            val name = ctx.stack.pop().asString()
            if (name.startsWith('@')) {
                val group = ctx.commands.getGroup(name.substring(1))
                if (group != null) {
                    for (cmd in group.commands) {
                        io.println("${cmd.name} - ${cmd.help.lines()[0]}")
                    }
                    io.println("Type `\"command\" help` for detailed info about a specific command.")
                } else {
                    io.println("There is no help topic called $name.")
                    io.println("Try something like this: \"@core\" help")
                }
            } else {
                val cmd = ctx.commands.get(name)
                if (cmd != null) {
                    io.println(cmd.help)
                } else {
                    io.println("There is no command called $name.")
                }
            }
        } else {
            io.println("Available help topics:")
            for (group in ctx.commands.iterGroups) {
                io.println("${group.name} - ${group.help.lines()[0]}")
            }
            io.println("Type `\"@topic\" help` for a list of commands in a specific category.")
            io.println("Type `\"command\" help` for detailed info about a specific command.")
        }
    }

    override val help: String
        get() = "Pops a string off the stack and prints information about the command with that name."
}
