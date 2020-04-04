package net.forkk.greenstone.grpl.commands

import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.FloatVal
import net.forkk.greenstone.grpl.IntVal
import net.forkk.greenstone.grpl.NullVal
import net.forkk.greenstone.grpl.StringVal
import net.forkk.greenstone.grpl.TypeError
import net.forkk.greenstone.grpl.ValueType

val TypeCommands = CommandGroup(
    "type",
    "Commands for converting between different types.",
    IntCmd, FloatCmd, StrCmd, NullCmd
)

object IntCmd : Command("int") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        when (v) {
            is IntVal -> ctx.stack.push(v)
            is FloatVal -> ctx.stack.push(IntVal(v.v.toInt()))
            is StringVal -> {
                val cast = v.v.toIntOrNull()
                ctx.stack.push(if (cast != null) IntVal(cast) else NullVal)
            }
            else -> throw TypeError(v, arrayOf(ValueType.INT, ValueType.FLOAT, ValueType.STRING))
        }
    }

    override val help: String
        get() = "Pops a value, converts it to an int, and pushes the int.\n" +
                "Converting a float to int will truncate it, meaning anything after the decimal point will be removed.\n" +
                "Converting a string to int will attempt to parse it, and return null if it is not a valid int.\n" +
                "No other types can be used with this command."
}

object FloatCmd : Command("float") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        when (v) {
            is FloatVal -> ctx.stack.push(v)
            is IntVal -> ctx.stack.push(FloatVal(v.v.toDouble()))
            is StringVal -> {
                val cast = v.v.toDoubleOrNull()
                ctx.stack.push(if (cast != null) FloatVal(cast) else NullVal)
            }
            else -> throw TypeError(v, arrayOf(ValueType.INT, ValueType.FLOAT, ValueType.STRING))
        }
    }

    override val help: String
        get() = "Pops a value, converts it to a float, and pushes the float.\n" +
                "Converting an int to float will convert to the same number as a float.\n" +
                "Converting a string to float will attempt to parse it, and return null if it is not a valid float.\n" +
                "No other types can be used with this command."
}

object StrCmd : Command("str") {
    override fun exec(ctx: Context) {
        ctx.stack.push(StringVal(ctx.stack.pop().displayStr()))
    }

    override val help: String
        get() = "Pops a value, converts it to a string, and pushes the string.\n" +
                "All types can be converted to string."
}

object NullCmd : Command("null") {
    override fun exec(ctx: Context) {
        ctx.stack.push(NullVal)
    }

    override val help: String
        get() = "Pushes null on the stack."
}
