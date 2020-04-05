package net.forkk.greenstone.grpl.commands

import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.IntVal
import net.forkk.greenstone.grpl.ListVal
import net.forkk.greenstone.grpl.StringVal
import net.forkk.greenstone.grpl.TypeError
import net.forkk.greenstone.grpl.ValueType

val StringCommands = CommandGroup(
    "string",
    "Commands for manipulating strings",
    SliceCmd, FindCmd, RFindCCmd, ConcatCmd
)

object SliceCmd : Command("slice") {
    override fun exec(ctx: Context) {
        val end = ctx.stack.pop().asIntOrErr()
        val start = ctx.stack.pop().asIntOrErr()
        val v = ctx.stack.pop()
        when (v) {
            is StringVal -> ctx.stack.push(StringVal(v.v.substring(start, end)))
            is ListVal -> ctx.stack.push(ListVal(v.lst.subList(start, end)))
            else -> throw TypeError(v, arrayOf(ValueType.STRING, ValueType.LIST))
        }
    }

    override val help: String
        get() = "Pops a string or list, a start index, and an end index, and pushes a sub-string or " +
                "sub-list defined by the start and end indices.\n" +
                "Example: `\"Hello\" 1 4 slice` pushes \"ello\""
}

object FindCmd : Command("find") {
    override fun exec(ctx: Context) {
        val find = ctx.stack.pop()
        val v = ctx.stack.pop()
        when (v) {
            is StringVal -> {
                val findstr = find.asString()
                ctx.stack.push(IntVal(v.v.indexOf(findstr)))
            }
            is ListVal -> ctx.stack.push(IntVal(v.lst.indexOf(find)))
            else -> throw TypeError(v, arrayOf(ValueType.STRING, ValueType.LIST))
        }
    }

    override val help: String
        get() = "Pops a string or list, and a pattern string or element to find, and returns the " +
                "index of the first occurrence of the given pattern or element in the list or string.\n" +
                "Pushes -1 if no match could be found.\n" +
                "EXample: `\"Hello hello\" \"ll\" find` pushes `2`"
}

object RFindCCmd : Command("rfind") {
    override fun exec(ctx: Context) {
        val find = ctx.stack.pop()
        val v = ctx.stack.pop()
        when (v) {
            is StringVal -> {
                val findstr = find.asString()
                ctx.stack.push(IntVal(v.v.lastIndexOf(findstr)))
            }
            is ListVal -> ctx.stack.push(IntVal(v.lst.lastIndexOf(find)))
            else -> throw TypeError(v, arrayOf(ValueType.STRING, ValueType.LIST))
        }
    }

    override val help: String
        get() = "Pops a string or list, and a pattern string or element to find, and returns the " +
                "index of the last occurrence of the given pattern or element in the list or string.\n" +
                "Pushes -1 if no match could be found.\n" +
                "EXample: `\"Hello hello\" \"ll\" find` pushes `8`"
}

object ConcatCmd : Command("concat") {
    override fun exec(ctx: Context) {
        val b = ctx.stack.pop()
        val a = ctx.stack.pop()
        when (a) {
            is StringVal -> ctx.stack.push(StringVal(a.v + b.asString()))
            is ListVal -> ctx.stack.push(ListVal(a.lst + b.asListOrErr()))
            else -> throw TypeError(a, arrayOf(ValueType.STRING, ValueType.LIST))
        }
    }

    override val help: String
        get() = "Pops two strings or lists and pushes the concatenation of the two.\n" +
                "If one is a list and the other a string, or either is some other type, raises a type error."
}
