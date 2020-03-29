package net.forkk.greenstone.grpl

/**
 * Represents a set of built-in commands that the interpreter can use.
 */
class CommandSet {
    private var map = hashMapOf<String, Command>()

    fun get(name: String): Command? {
        return map[name]
    }

    fun addCommands(vararg cmds: Command) {
        cmds.forEach { c ->
            this.map[c.name] = c
        }
    }
}

/**
 * The default set of built in commands.
 *
 * Theoretically we could allow other mods to extend this later.
 */
val baseCmds: CommandSet = {
    val cmdSet = CommandSet()
    cmdSet.addCommands(PopCmd, DupCmd, SwapCmd)
    cmdSet.addCommands(NotCmd, AndCmd, OrCmd)
    cmdSet.addCommands(GtCmd, LtCmd)
    cmdSet.addCommands(AddCmd, SubCmd, MulCmd, DivCmd, IDivCmd)
    cmdSet
}()

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
        get() = "Pops the boolean value on the top of the stack and pushes the opposite."
}
object AndCmd : Command("and") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop().asBoolOrErr()
        val b = ctx.stack.pop().asBoolOrErr()
        ctx.stack.push(BoolVal(a && b))
    }

    override val help: String
        get() = "Pops two values and pushes true if both are true, otherwise pushes false."
}
object OrCmd : Command("or") {
    override fun exec(ctx: Context) {
        val a = ctx.stack.pop().asBoolOrErr()
        val b = ctx.stack.pop().asBoolOrErr()
        ctx.stack.push(BoolVal(a || b))
    }

    override val help: String
        get() = "Pops two values and pushes false if both are false, otherwise pushes true."
}

object GtCmd : Command("gt") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntCmpOp(av, bv, { a, b -> a > b }, { a, b -> a > b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops the top two elements off the stack, compares them, and returns true if the bottom one is " +
                "greater than the top."
}
object LtCmd : Command("lt") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntCmpOp(av, bv, { a, b -> a < b }, { a, b -> a < b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Pops the top two elements off the stack, compares them, and returns true if the bottom one is " +
                "less than the top."
}

object AddCmd : Command("add") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a + b }, { a, b -> a + b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Adds top two numbers on the stack. Raises type error if values are not numbers."
}
object SubCmd : Command("sub") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a - b }, { a, b -> a - b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Subtracts numbers on the stack. For example, if the stack is `a b` (a pushed first), this pushes a - b. " +
                "Raises type error if the values are not numbers."
}
object MulCmd : Command("mul") {
    override fun exec(ctx: Context) {
        val bv = ctx.stack.pop()
        val av = ctx.stack.pop()
        val result = floatIntBinOp(av, bv, { a, b -> a * b }, { a, b -> a * b })
        ctx.stack.push(result)
    }

    override val help: String
        get() = "Multiplies numbers on the stack. Raises type error if the values are not numbers."
}
object DivCmd : Command("div") {
    override fun exec(ctx: Context) {
        val b = ctx.stack.pop().asFloatOrErr()
        val a = ctx.stack.pop().asFloatOrErr()
        ctx.stack.push(FloatVal(a / b))
    }

    override val help: String
        get() = "Performs floating point division. If the operands are integers, they will be cast to floats before division. " +
                "Ordering is the same as subtraction, so if the stack is `a b` (a pushed first), this pushes a / b. " +
                "Raises type error if the values are not numbers."
}
object IDivCmd : Command("idiv") {
    override fun exec(ctx: Context) {
        val b = ctx.stack.pop().asIntOrErr()
        val a = ctx.stack.pop().asIntOrErr()
        ctx.stack.push(IntVal(a / b))
    }

    override val help: String
        get() = "Performs integer division. If the operands are floats, they will be cast to integers before division. " +
                "Ordering is the same as subtraction, so if the stack is `a b` (a pushed first), this pushes a / b. " +
                "Raises type error if the values are not numbers."
}
