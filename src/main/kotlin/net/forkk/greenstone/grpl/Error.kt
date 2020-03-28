package net.forkk.greenstone.grpl

abstract class ExecError(msg: String) : Exception(msg)

class EmptyStackError : ExecError("Tried to pop empty stack")
class TypeError(val value: Value, val expected: ValueType) : ExecError(
        "Expected type $expected, but value $value had type ${value.type}."
)
class UndefinedNameError(val name: String) : ExecError("Variable name $name is not defined.")
