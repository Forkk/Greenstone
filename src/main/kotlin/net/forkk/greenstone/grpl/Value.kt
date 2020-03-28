package net.forkk.greenstone.grpl

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
    if (av.isInt() && bv.isInt()) {
        return IntVal(intFn(av.asInt()!!, bv.asInt()!!))
    } else {
        val a = av.asFloat()
        val b = bv.asFloat()
        when {
            a == null -> { throw TypeError(av, ValueType.FLOAT) }
            b == null -> { throw TypeError(bv, ValueType.FLOAT) }
            else -> { return FloatVal(floatFn(a, b)) }
        }
    }
}

/**
 * Used to represent the type of a value as a value.
 */
enum class ValueType {
    INT, FLOAT,
    STRING, LIST,
    TYPE, NULL;
}

/**
 * Represents all possible values in the GRPL language.
 *
 * This is a closed set of classes which represent the various types of value which can exist within the language.
 */
sealed class Value(val type: ValueType) {
    open fun isNull(): Boolean { return false }

    open fun isInt(): Boolean { return false }
    /** Casts value as an int. Returns null if this type doesn't convert to int. */
    open fun asInt(): Int? { return null }
    /** Variant of `asInt` which throws `TypeError` instead of returning null. */
    fun asIntOrErr(): Int {
        val v = asInt()
        if (v == null) { throw TypeError(this, ValueType.INT) } else { return v }
    }

    open fun isFloat(): Boolean { return false }
    /** Casts value as float. Returns null if this type doesn't convert to float. */
    open fun asFloat(): Double? { return null }
    /** Variant of `asFloat` which throws `TypeError` instead of returning null. */
    fun asFloatOrErr(): Double {
        val v = asFloat()
        if (v == null) { throw TypeError(this, ValueType.FLOAT) } else { return v }
    }

    /**
     * The length of this value if it can be considered to have a length (for example, if it is a string or list).
     *
     * If the value doesn't have a meaningful length, returns null.
     */
    open val length: Int?
        get() = null
}

object NullVal : Value(ValueType.NULL) {
    override fun isNull(): Boolean { return true }
}

data class IntVal(public val v: Int) : Value(ValueType.INT) {
    override fun isInt(): Boolean { return true }
    override fun asInt(): Int? { return this.v }
    override fun asFloat(): Double? { return this.v.toDouble() }
}
data class FloatVal(public val v: Double) : Value(ValueType.FLOAT) {
    override fun isFloat(): Boolean { return true }
    override fun asFloat(): Double? { return this.v }
    override fun asInt(): Int? { return this.v.toInt() }
}

data class StringVal(public val v: String) : Value(ValueType.STRING) {
    override val length: Int?
        get() = v.length
}

data class ListVal(public val lst: List<Value>) : Value(ValueType.LIST) {
    override val length: Int?
        get() = lst.size
}

/**
 * Represents the type of some value as a value.
 */
data class TypeVal(val t: ValueType) : Value(ValueType.TYPE)
