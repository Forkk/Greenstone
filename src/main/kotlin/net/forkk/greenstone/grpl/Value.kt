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
    return if (av.isType(ValueType.INT) && bv.isType(ValueType.INT)) {
        IntVal(intFn(av.asIntOrErr(), bv.asIntOrErr()))
    } else {
        FloatVal(floatFn(av.asFloatOrErr(), bv.asFloatOrErr()))
    }
}

/**
 * Used to represent the type of a value as a value.
 */
enum class ValueType {
    INT, FLOAT,
    STRING, LIST,
    BOOL,
    TYPE, NULL;
}

/**
 * Represents all possible values in the GRPL language.
 *
 * This is a closed set of classes which represent the various types of value which can exist
 * within the language.
 */
sealed class Value(val type: ValueType) {
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

data class BoolVal(val v: Boolean) : Value(ValueType.BOOL) {
    override fun asBool(): Boolean? { return this.v }
}

data class IntVal(val v: Int) : Value(ValueType.INT) {
    override fun asInt(): Int? { return this.v }
    override fun asFloat(): Double? { return this.v.toDouble() }
}
data class FloatVal(val v: Double) : Value(ValueType.FLOAT) {
    override fun asFloat(): Double? { return this.v }
    override fun asInt(): Int? { return this.v.toInt() }
}

data class StringVal(val v: String) : Value(ValueType.STRING) {
    override val length: Int?
        get() = v.length
}

data class ListVal(val lst: List<Value>) : Value(ValueType.LIST) {
    override val length: Int?
        get() = lst.size
}

/**
 * Represents the type of some value as a value.
 */
data class TypeVal(val t: ValueType) : Value(ValueType.TYPE)
