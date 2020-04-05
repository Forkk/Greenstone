package net.forkk.greenstone.grpl.commands

import net.forkk.greenstone.grpl.Context
import net.forkk.greenstone.grpl.IndexError
import net.forkk.greenstone.grpl.IntVal
import net.forkk.greenstone.grpl.ListVal
import net.forkk.greenstone.grpl.TypeError
import net.forkk.greenstone.grpl.Value
import net.forkk.greenstone.grpl.ValueType

val ListCommands = CommandGroup(
    "list",
    "Commands for manipulating lists",
    NewListCmd, ToListCmd, LenCmd, GetCmd, AppendCmd, RemoveCmd, InsertCmd
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
        val list = mutableListOf<Value>()
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

object GetCmd : Command("listget") {
    override fun exec(ctx: Context) {
        val idx = ctx.stack.pop().asIntOrErr()
        val lst = ctx.stack.pop().asListOrErr()
        ctx.stack.push(lst.getOrNull(idx) ?: throw IndexError(idx, 0..lst.size))
    }

    override val help: String
        get() = "Pops a list and an integer index and pushes the element of the list at the given index.\n" +
                "Lists are 0-indexed, meaning the first element has index 0.\n" +
                "Example: `newlist 1 2 3 tolist 1 listget` would push `2`"
}

object RemoveCmd : Command("listrm") {
    override fun exec(ctx: Context) {
        val idx = ctx.stack.pop().asIntOrErr()
        val lst: MutableList<Value> = ctx.stack.pop().asListOrErr().toMutableList()
        if (idx >= lst.size) throw IndexError(idx, 0..lst.size)
        lst.removeAt(idx)
        ctx.stack.push(ListVal(lst))
    }

    override val help: String
        get() = "Pops a list and an integer index and pushes the same list with the element at the given index removed.\n" +
                "Example `newlist 1 2 3 tolist 1 listrm` would push the list `[1, 3]`."
}

object InsertCmd : Command("listinsert") {
    override fun exec(ctx: Context) {
        val idx = ctx.stack.pop().asIntOrErr()
        val elem = ctx.stack.pop()
        val lst: MutableList<Value> = ctx.stack.pop().asListOrErr().toMutableList()
        if (idx >= lst.size) throw IndexError(idx, 0..lst.size)
        lst.add(idx, elem)
        ctx.stack.push(ListVal(lst))
    }

    override val help: String
        get() = "Pops a list, a value, and an integer index, and pushes the list with the value inserted at the given " +
                "index.\n" +
                "Example: `newlist 1 2 3 tolist 4 1 listinsert` would push the list `[1, 4, 2, 3]`."
}

object AppendCmd : Command("listappend") {
    override fun exec(ctx: Context) {
        val v = ctx.stack.pop()
        val lst = ctx.stack.pop().asListOrErr()
        ctx.stack.push(ListVal(lst + v))
    }

    override val help: String
        get() = "Pops a list and an element and then pushes the list with the element on the end.\n" +
                "Example: `newlist 1 2 tolist 3 append` will leave list [1, 2, 3] on the stack."
}
