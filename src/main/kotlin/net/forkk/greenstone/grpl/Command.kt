package net.forkk.greenstone.grpl

/**
 * Represents a set of built-in commands that the interpreter can use.
 */
class CommandSet(vararg group: CommandGroup) {
    private var map = hashMapOf<String, Command>()
    private var _groups = hashMapOf<String, CommandGroup>()

    init {
        group.forEach { addGroup(it) }
    }

    /** Gets a command from the set. */
    fun get(name: String): Command? {
        return map[name]
    }

    /** Adds a new command group to this set. */
    fun addGroup(group: CommandGroup) {
        if (group.name in _groups) {
            error("A command group named ${group.name} already exists")
        }
        _groups[group.name] = group
        group.commands.forEach { c ->
            this.map[c.name] = c
        }
    }

    /** Gets the command group with the given name. */
    fun getGroup(name: String): CommandGroup? = _groups[name]

    /** An iterator over the command groups. */
    val iterGroups: Iterable<CommandGroup> get() = _groups.values

    /**
     * Merges another command set into this one.
     *
     * Commands in `other` override commands that are present in both.
     */
    fun merge(other: CommandSet) {
        this.map.putAll(other.map)
    }

    val list: Iterable<Command> get() = this.map.values
}

/**
 * The default set of built in commands.
 *
 * Theoretically we could allow other mods to extend this later.
 */
fun baseCmds(extra: List<CommandGroup> = listOf()): CommandSet {
    val cmdSet = CommandSet(CoreCommands, MathCommands)
    extra.forEach { cmdSet.addGroup(it) }
    return cmdSet
}

/**
 * Represents a built-in command in the GRPL language.
 */
abstract class Command(val name: String) {
    /**
     * Executes this command in the given context.
     */
    abstract fun exec(ctx: Context)

    /** A string describing to the player how to use this command. */
    abstract val help: String
}

/**
 * Represents a logical group of commands used to group commands when displayed in the help menu.
 */
class CommandGroup(val name: String, val help: String, vararg _cmds: Command) {
    val commands = listOf<Command>(*_cmds)
}


val CoreCommands = CommandGroup("core",
    "Core language commands for manipulating the stack, performing comparisons, etc.",
    PopCmd, DupCmd, SwapCmd,
    NotCmd, AndCmd, OrCmd, EqCmd
)

object PopCmd : Command("pop") {
    override fun exec(ctx: Context) { ctx.stack.pop() }
    override val help: String
        get() = "Discards the item at the top of the stack."
}
object DupCmd : Command("dup") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        ctx.stack.push(v)
        ctx.stack.push(v)
    }

    override val help: String
        get() = "Duplicates the item at the top of the stack."
}
object SwapCmd : Command("swap") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop()
        val b = ctx.stack.pop()
        ctx.stack.push(a)
        ctx.stack.push(b)
    }

    override val help: String
        get() = "Swaps the two items at the top of the stack."
}

object NotCmd : Command("not") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop().asBoolOrErr()
        ctx.stack.push(BoolVal(!a))
    }

    override val help: String
        get() = "Pops a boolean value and pushes the opposite."
}
object AndCmd : Command("and") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop().asBoolOrErr()
        val b = ctx.stack.pop().asBoolOrErr()
        ctx.stack.push(BoolVal(a && b))
    }

    override val help: String
        get() = "Pops two values and pushes true if both are true, otherwise false."
}
object OrCmd : Command("or") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop().asBoolOrErr()
        val b = ctx.stack.pop().asBoolOrErr()
        ctx.stack.push(BoolVal(a || b))
    }

    override val help: String
        get() = "Pops two values and pushes false if both are false, otherwise true."
}

object EqCmd : Command("eq") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        ctx.stack.push(BoolVal(av == bv))
    }

    override val help: String
        get() = "Pops two values, and pushes true if they are equal, false if they are not.\n" +
                "Values are considered equal, if and only if their types and values are equal. " +
                "No type casting is done.\n" +
                "Example: `2 2 eq` would push `true` on the stack, but `2.0 2 eq` will push `false`, as one value is " +
                "an integer, while the other is a float."
}
