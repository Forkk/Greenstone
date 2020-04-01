package net.forkk.greenstone.grpl

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.TestOnly

/**
 * Source location for programs entered interactively, for example in the terminal or in a unit test.
 */
@Serializable
open class SourceLocation(val input: String, val pos: Pair<Int, Int>) {
    companion object {
        /**
         * Converts a parser `TokenMatch` into a `SourceLocation`
         */
        fun fromTokenMatch(input: String, match: TokenMatch): SourceLocation {
            return SourceLocation(input, Pair(match.position, match.position + match.text.length))
        }
    }

    /**
     * The entire line of source code the associated statement occurred on.
     */
    val sourceLine: String get() = input.substring(lineStart, lineEnd)

    /**
     * A tuple containing the start and end position within the line.
     */
    val linePos: Pair<Int, Int>

    private val lineStart: Int
    private val lineEnd: Int

    init {
        val (start, end) = pos
        // Find the newline before and after the start pos
        lineStart = kotlin.math.max(0, input.lastIndexOf('\n', start))
        var lEnd = input.indexOf('\n', end)
        if (lEnd == -1) { lEnd = input.length }
        lineEnd = lEnd
        linePos = Pair(start - lineStart, end - lineStart)
    }

    override fun toString(): String = "Position ${linePos.first}, ${linePos.second} in line $sourceLine"

    /**
     * Returns a two line string, with this location's `sourceLine` on the first line, and an underline on the second.
     */
    fun underline(): String {
        val spaces = " ".repeat(linePos.first)
        val line = "^".repeat(linePos.second - linePos.first)
        return "$sourceLine\n$spaces$line"
    }
}

/**
 * For unit testing only. Equality comparisons always return true when compared against other `SourceLocation`s.
 *
 * Used to test the parser results without worrying about `SourceLocation`s.
 */
@TestOnly
@Serializable
class TestSourceLocation() : SourceLocation("", Pair(0, 0)) {
    override fun equals(other: Any?): Boolean {
        return other is SourceLocation
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
