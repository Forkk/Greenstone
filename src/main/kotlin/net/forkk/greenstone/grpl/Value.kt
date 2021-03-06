package net.forkk.greenstone.grpl

import kotlinx.serialization.Serializable

/**
 * Takes an `a` and `b` value, and performs the `intFn` if both are ints. Otherwise, casts to float and performs
 * `floatFn`. If either value cannot be cast to float, raises `TypeError`.
 *
 * @param a value to perform op on
 * @param b other value to perform op on
 * @param floatFn operation to perform if values are cast to float
 * @param intFn operation to perform if both values are ints
 */
fun floatIntBinOp(
    av: Value,
    bv: Value,
    floatFn: (Double, Double) -> Double,
    intFn: (Int, Int) -> Int
): Value {
    return if (av.isType(ValueType.INT) && bv.isType(ValueType.INT)) {
        IntVal(intFn(av.asIntOrErr(), bv.asIntOrErr()))
    } else {
        FloatVal(floatFn(av.asFloatOrErr(), bv.asFloatOrErr()))
    }
}

/**
 * Similar to `floatIntBinOp`, but returns a bool
 *
 * @param a value to perform op on
 * @param b other value to perform op on
 * @param floatFn operation to perform if values are cast to float
 * @param intFn operation to perform if both values are ints
 */
fun floatIntCmpOp(
    av: Value,
    bv: Value,
    floatFn: (Double, Double) -> Boolean,
    intFn: (Int, Int) -> Boolean
): Value {
    return if (av.isType(ValueType.INT) && bv.isType(ValueType.INT)) {
        BoolVal(intFn(av.asIntOrErr(), bv.asIntOrErr()))
    } else {
        BoolVal(floatFn(av.asFloatOrErr(), bv.asFloatOrErr()))
    }
}

/**
 * Used to represent the type of a value as a value.
 */
@Serializable
enum class ValueType {
    INT, FLOAT,
    STRING, LIST,
    BOOL, FUN,
    TYPE, NULL;
}

/**
 * Represents all possible values in the GRPL language.
 *
 * This is a closed set of classes which represent the various types of value which can exist
 * within the language.
 */
@Serializable
sealed class Value(
    val type: ValueType
) {
    open fun isNull(): Boolean { return false }

    /** Checks if this value has the given type. */
    fun isType(t: ValueType): Boolean { return this.type == t }

    /** Casts value as an int. Returns null if this type doesn't convert to int. */
    open fun asInt(): Int? { return null }
    /** Variant of `asInt` which throws `TypeError` instead of returning null. */
    fun asIntOrErr(): Int {
        val v = asInt()
        if (v == null) { throw TypeError(this, ValueType.INT) } else { return v }
    }

    /** Casts value as float. Returns null if this type doesn't convert to float. */
    open fun asFloat(): Double? { return null }
    /** Variant of `asFloat` which throws `TypeError` instead of returning null. */
    fun asFloatOrErr(): Double {
        val v = asFloat()
        if (v == null) { throw TypeError(this, ValueType.FLOAT) } else { return v }
    }

    /** Casts this as a boolean. Returns null if the type doesn't convert to boolean. */
    open fun asBool(): Boolean? { return null }
    /** Variant of `asBool` which throws `TypeError` instead of returning null. */
    fun asBoolOrErr(): Boolean {
        val v = asBool()
        if (v == null) { throw TypeError(this, ValueType.BOOL) } else { return v }
    }

    /** Casts this as a function body. Throws a type error if this isn't a function. */
    open fun asFun(): List<Statement> { throw TypeError(this, ValueType.FUN) }

    /** Casts this as a string. Throws a type error if this isn't a string. */
    open fun asString(): String { throw TypeError(this, ValueType.STRING) }

    /** Casts this as a list. Throws type error if this value isn't a list. */
    open fun asListOrErr(): List<Value> { throw TypeError(this, ValueType.LIST) }

    /** Like `toString`, but shows the "display" representation of the value.
     *
     * For example, for the int 1, this will return "1" instead of "IntVal(v=1)".
     */
    abstract fun displayStr(): String

    /**
     * The length of this value if it can be considered to have a length (for example, if it is a string or list).
     *
     * If the value doesn't have a meaningful length, returns null.
     */
    open val length: Int?
        get() = null
}

@Serializable
object NullVal : Value(ValueType.NULL) {
    override fun isNull(): Boolean { return true }
    override fun displayStr(): String = "null"
}

@Serializable
data class BoolVal(val v: Boolean) : Value(ValueType.BOOL) {
    override fun asBool(): Boolean? { return this.v }
    override fun displayStr(): String = this.v.toString()
}

@Serializable
data class IntVal(val v: Int) : Value(ValueType.INT) {
    override fun asInt(): Int? { return this.v }
    override fun asFloat(): Double? { return this.v.toDouble() }
    override fun displayStr(): String = this.v.toString()
}
@Serializable
data class FloatVal(val v: Double) : Value(ValueType.FLOAT) {
    override fun asFloat(): Double? { return this.v }
    override fun asInt(): Int? { return this.v.toInt() }
    override fun displayStr(): String = this.v.toString()
}

@Serializable
data class StringVal(val v: String) : Value(ValueType.STRING) {
    override fun asString(): String = this.v
    override val length: Int? get() = v.length
    override fun displayStr(): String = this.v
}

@Serializable
data class ListVal(val lst: List<Value>) : Value(ValueType.LIST) {
    override val length: Int? get() = lst.size
    override fun asListOrErr(): List<Value> = this.lst
    override fun displayStr(): String = this.lst.toString()
}

@Serializable
data class FunVal(val body: List<Statement>) : Value(ValueType.FUN) {
    override fun asFun(): List<Statement> { return body }
    override fun displayStr(): String = "fun"
    override fun toString(): String = "FunVal(body=...)"
}

/**
 * Represents the type of some value as a value.
 */
@Serializable
data class TypeVal(val t: ValueType) : Value(ValueType.TYPE) {
    override fun displayStr(): String = this.t.toString()
}
