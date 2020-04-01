package net.forkk.greenstone.grpl

val MathCommands = CommandGroup("math",
    "Commands for mathematical operations",
    GtCmd, LtCmd,
    AddCmd, SubCmd,
    MulCmd, DivCmd, IDivCmd
)

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
