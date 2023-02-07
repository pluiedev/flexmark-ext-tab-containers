package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.util.sequence.BasedSequence
import java.util.regex.Matcher
import java.util.regex.Pattern

data class Entry(
    val lang: Language,
    val link: String,
    val content: String,
)
typealias SectionPredicate = (input: String, section: String) -> Int
data class Language(
    val id: String,
    val displayName: String? = null,
    val extension: String = id,
    val sectionStartPredicate: SectionPredicate = { input, section ->
        val s = "//@start $section"
        input.indexOf(s) + s.length
    },
    val sectionEndPredicate: SectionPredicate = { input, section ->
        input.indexOf("//@end $section")
    },
) {
    fun extractSection(input: String, section: String): Section? {
        if (section == "*") {
            return Section(input, 1..input.lineNumber)
        }

        val start = sectionStartPredicate(input, section).also {
            if (it < 0) return null
        }
        val end = sectionEndPredicate(input, section).also {
            if (it < 0) return null
        }
        val startLine = input.substring(0 until start).lineNumber + 1
        val endLine = input.substring(0 until end).lineNumber - 1

        val text = input.substring(start until end).trimIndent().trim()

        return Section(text, startLine..endLine)
    }
}
data class Section(
    val content: String,
    val lineRange: IntRange,
)

internal class MatchScope(val input: BasedSequence, val matcher: Matcher) {
    fun subSequenceOfGroup(group: Int): BasedSequence =
        if (matcher.start(group) != -1)
            input.subSequence(matcher.start(group), matcher.end(group))
        else
            BasedSequence.NULL
}


internal inline fun <R> Pattern.match(input: BasedSequence, action: MatchScope.() -> R): R? =
    MatchScope(input, matcher(input)).let {
        if (it.matcher.find()) it.action() else null
    }

private inline val String.lineNumber get() = count { it == '\n' } + 1