package net.forkk.greenstone.grpl

abstract class ExecError(msg: String) : Exception(msg) {
    val trace = ErrorTrace()

    fun prettyMsg(): String {
        val location = this.trace.location
        val loc = if (location != null) {
            "\nError occurred here:\n${location.underline()}"
        } else { "" }
        return "Execution error: ${this.message}$loc"
    }
}

private fun typeErrMsg(value: Value, expected: Array<ValueType>): String {
    val expectedLst = expected.joinToString(", ")
    return "Expected type $expectedLst, but value $value had type ${value.type}."
}
class TypeError(val value: Value, val expected: Array<ValueType>) : ExecError(
    typeErrMsg(value, expected)
) {
    constructor(value: Value, expected: ValueType) : this(value, arrayOf(expected))
}

class EmptyStackError() : ExecError("Tried to pop empty stack")
class UnknownCommandError(val name: String) : ExecError("There is no command named $name.")
class UndefinedNameError(val name: String) : ExecError("Variable name $name is not defined.")
class ArithmeticError(msg: String = "Invalid arithmetic operation") : ExecError(msg)
class IndexError(val idx: Int, val range: IntRange) :
    ExecError("Index $idx out of range ${range.first}, ${range.last}")
class FileError(msg: String = "A file IO error occurred") : ExecError(msg)

// For now just a source location, but this will be used for stack traces later.
class ErrorTrace {
    private var _loc: SourceLocation? = null

    /**
     * Location of the statement this error occurred at.
     */
    val location: SourceLocation? get() = _loc

    /**
     * Tags the location this error occurred at. If one is already recorded, this does nothing.
     */
    fun tagLocation(loc: SourceLocation) {
        if (_loc == null) _loc = loc
    }
}
