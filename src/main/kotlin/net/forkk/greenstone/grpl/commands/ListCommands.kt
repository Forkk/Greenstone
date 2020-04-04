package net.forkk.greenstone.grpl.commands

import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.IntVal
import net.forkk.greenstone.grpl.ListVal
import net.forkk.greenstone.grpl.TypeError
import net.forkk.greenstone.grpl.Value
import net.forkk.greenstone.grpl.ValueType

val ListCommands = CommandGroup(
    "list",
    "Commands for manipulating lists",
    NewListCmd, ToListCmd, LenCmd, AppendCmd
)

object NewListCmd : Command("newlist") {
    override fun exec(ctx: Context) {
        ctx.stack.push(ListVal(listOf()))
    }

    override val help: String
        get() = "Pushes a new, empty list on the stack."
}

object ToListCmd : Command("tolist") {
    override fun exec(ctx: Context) {
        var list = listOf<Value>()
        var v = ctx.stack.pop()
        while (v !is ListVal) {
            list += v
            v = ctx.stack.pop()
        }
        ctx.stack.push(ListVal(v.lst + list.reversed()))
    }

    override val help: String
        get() = "Pops values from the stack repeatedly until a list is popped, " +
                "and then adds all of the values popped to the list." +
                "Useful for building lists easily.\n" +
                "Example: `newlist 1 2 3 tolist` will leave the list [1, 2, 3] on the stack."
}

object LenCmd : Command("len") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        val len = v.length
        if (len == null) throw TypeError(v, arrayOf(ValueType.STRING, ValueType.LIST))
        else ctx.stack.push(IntVal(len))
    }

    override val help: String
        get() = "Pops a value and pushes its length.\n" +
                "This only works for values that have a meaningful length, such as strings and " +
                "lists. For other values, this results in a TypeError."
}

object AppendCmd : Command("append") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        val lst = ctx.stack.pop()
        if (lst is ListVal) {
            ctx.stack.push(ListVal(lst.lst + v))
        } else {
            throw TypeError(lst, ValueType.LIST)
        }
    }

    override val help: String
        get() = "Pops a list and an element and then pushes the list with the element on the end.\n" +
                "Example: `newlist 1 2 tolist 3 append` will leave list [1, 2, 3] on the stack."
}
