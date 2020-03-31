package net.forkk.greenstone.grpl

import kotlinx.serialization.Serializable

/**
 * Represents a set of built-in commands that the interpreter can use.
 */
@Serializable
class CommandSet {
    constructor(vararg cmds: Command) {
        cmds.forEach { c ->
            this.map[c.name] = c
        }
    }

    private var map = hashMapOf<String, Command>()

    fun get(name: String): Command? {
        return map[name]
    }

    /**
     * Adds commands to this set.
     */
    fun addCommands(vararg cmds: Command) {
        cmds.forEach { c ->
            this.map[c.name] = c
        }
    }

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
fun baseCmds(extra: CommandSet? = null): CommandSet {
    val cmdSet = CommandSet()
    cmdSet.addCommands(PopCmd, DupCmd, SwapCmd)
    cmdSet.addCommands(NotCmd, AndCmd, OrCmd)
    cmdSet.addCommands(EqCmd, GtCmd, LtCmd)
    cmdSet.addCommands(AddCmd, SubCmd, MulCmd, DivCmd, IDivCmd)
    if (extra != null) cmdSet.merge(extra)
    return cmdSet
}

/**
 * Represents a built-in command in the GRPL language.
 */
@Serializable
abstract class Command(val name: String) {
    /**
     * Executes this command in the given context.
     */
    abstract fun exec(ctx: Context)

    /** A string describing to the player how to use this command. */
    abstract val help: String
}

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
object GtCmd : Command("gt") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntCmpOp(av, bv, { a, b -> a > b }, { a, b -> a > b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops two values, compares them, and pushes true if the top one is greater than the bottom one.\n" +
                "Example: `2 3 gt` would leave `true` on the stack"
}
object LtCmd : Command("lt") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntCmpOp(av, bv, { a, b -> a < b }, { a, b -> a < b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops two values, compares them, and pushes true if the top one is less than the bottom one.\n" +
                "Example: `3 2 lt` would leave `true` on the stack"
}

object AddCmd : Command("add") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a + b }, { a, b -> a + b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops two values and pushes their sum.\n" +
                "Raises a type error if the values are not numbers."
}
object SubCmd : Command("sub") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a - b }, { a, b -> a - b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops two values and subtracts the top one from the bottom one.\n" +
                "Example: if the stack is `a b` (a pushed first), this pushes a - b.\n" +
                "Raises a type error if the values are not numbers."
}
object MulCmd : Command("mul") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a * b }, { a, b -> a * b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops two values and pushes their product.\n" +
                "Raises a type error if the values are not numbers."
}
object DivCmd : Command("div") {
    override fun exec(ctx: Context) {
        val b = ctx.stack.pop().asFloatOrErr()
        val a = ctx.stack.pop().asFloatOrErr()
        ctx.stack.push(FloatVal(a / b))
    }

    override val help: String
        get() = "Pops two values and divides the bottom one by the top one.\n" +
                "Example: if the stack is `a b` (a pushed first), this pushes a / b.\n" +
                "Raises a type error if the values are not numbers."
}
object IDivCmd : Command("idiv") {
    override fun exec(ctx: Context) {
        val b = ctx.stack.pop().asIntOrErr()
        val a = ctx.stack.pop().asIntOrErr()
        if (b == 0) throw ArithmeticError("Attempted to divide $a by 0.")
        ctx.stack.push(IntVal(a / b))
    }

    override val help: String
        get() = "Pops two values and divides bottom by top using integer division. " +
                "This means float values are cast to int, before division, and the returned value is an int.\n" +
                "Example: if the stack is `a b` (a pushed first), this pushes a / b.\n" +
                "Raises a type error if the values are not numbers."
}
