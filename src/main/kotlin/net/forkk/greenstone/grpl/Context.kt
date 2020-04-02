package net.forkk.greenstone.grpl

import kotlinx.serialization.Serializable

/**
 * Context data that gets saved in the world file. Commands are excluded, since they are re-initialized on loading.
 */
@Serializable
data class ContextSaveData(
    val vars: HashMap<String, Value> = HashMap(),
    val stack: Stack = Stack()
) {
    /**
     * Takes a set of optional extra commands and initializes a new contexxt from this save state.
     */
    fun toContext(extraCmds: List<CommandGroup> = listOf()): Context {
        return Context(extraCmds, vars, stack)
    }
}

/**
 * An execution context for a GRPL program. Contains the stack and variable store.
 */
class Context(
    extraCmds: List<CommandGroup> = listOf(),
    private val vars: HashMap<String, Value> = hashMapOf(),
    val stack: Stack = Stack()
) {
    val commands = baseCmds(extraCmds)

    /** Gets a save data object to store this context in the world file. */
    val saveData: ContextSaveData get() = ContextSaveData(vars, stack)

    /**
     * Executes a list of statements in this context.
     */
    fun exec(stmts: List<Statement>) {
        for (stmt in stmts) {
            try {
                stmt.exec(this)
            } catch (e: ExecError) {
                // If there is an error executing the statement, tag the error with the statement's location information,
                // and then throw it up the call stack.
                // We ignore the location of things like `while` statements and `if` statements, as we are only really
                // concerned with the location of the statements inside them.
                if (stmt is LocatedStatement) {
                    e.trace.tagLocation(stmt.location)
                }
                throw e
            }
        }
    }

    /**
     * Gets the variable `name`.
     *
     * Throws an `UndefinedNameError if the given name doesn't exist.
     */
    fun getVar(name: String): Value {
        val v = vars[name]
        if (v == null) { throw UndefinedNameError(name) } else { return v }
    }

    /**
     * Sets the variable `name` to the value `v`.
     */
    fun setVar(name: String, v: Value) {
        vars[name] = v
    }
}

/**
 * A value stack used for program execution.
 */
@Serializable
class Stack {
    private val _stack = mutableListOf<Value>()

    /** A read-only list of elements in the stack */
    val list: List<Value> get() = _stack

    /** Pushes a value on the stack */
    fun push(v: Value) {
        _stack.add(v)
    }

    /**
     * Pops a value from the stack and returns it.
     *
     * If the stack is empty, throws `EmptyStackError`.
     */
    fun pop(): Value {
        return if (_stack.size > 0) {
            _stack.removeAt(_stack.size - 1)
        } else {
            throw EmptyStackError()
        }
    }

    /**
     * Returns the value at the top of the stack without removing it.
     *
     * If the stack is empty, returns null.
     */
    fun peek(): Value? {
        return if (_stack.size > 0) {
            _stack[_stack.size - 1]
        } else {
            null
        }
    }

    /** Clears all elements in the stack */
    fun clear() { _stack.clear() }

    /** Number of elements on the stack */
    val size get() = _stack.size
}
