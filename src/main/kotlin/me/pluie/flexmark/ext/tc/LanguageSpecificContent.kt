package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.lang.StringBuilder

class LanguageSpecificContent: Block() {
    var openingMarker: BasedSequence = BasedSequence.NULL
    var languages: BasedSequence = BasedSequence.NULL
    var closingMarker: BasedSequence = BasedSequence.NULL

    override fun getSegments() = arrayOf(openingMarker, languages, closingMarker)

    override fun getAstExtra(out: StringBuilder) {
        segmentSpanChars(out, openingMarker, "openingMarker")
        segmentSpanChars(out, languages, "languages")
        segmentSpanChars(out, closingMarker, "closingMarker")
    }

    override fun toString() = toAstString(true)
}