package me.pluie.flexmark.ext.tc.internal

import com.vladsch.flexmark.parser.block.*
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.data.DataHolder
import me.pluie.flexmark.ext.tc.*
import java.util.regex.Pattern

class TabContainerParser(private val block: TabContainer): AbstractBlockParser() {
    override fun getBlock(): Block = block

    override fun isContainer(): Boolean = true
    override fun canContain(state: ParserState, blockParser: BlockParser, block: Block): Boolean
        = block is LanguageSpecificContent

    override fun tryContinue(state: ParserState): BlockContinue? {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line

        if (state.indent <= 3 && nextNonSpace < line.length) {
            val trySequence = line.subSequence(nextNonSpace, line.length)

            return CLOSING_PATTERN.match(trySequence) {
                block.closingMarker = subSequenceOfGroup(1)
                BlockContinue.finished()
            } ?: BlockContinue.atIndex(nextNonSpace)
        }
        return BlockContinue.atIndex(nextNonSpace)
    }

    override fun closeBlock(state: ParserState) {
        block.setCharsFromContent()
    }

    object Factory: CustomBlockParserFactory {
        override fun apply(options: DataHolder): BlockParserFactory =
            ParserFactory(options)

        override fun getAfterDependents(): MutableSet<Class<*>>? = null
        override fun getBeforeDependents(): MutableSet<Class<*>>? = null

        override fun affectsGlobalScope(): Boolean = false
    }
    class ParserFactory(private val options: DataHolder) : AbstractBlockParserFactory(options) {
        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
            if (state.indent >= 4) return BlockStart.none()

            val nextNonSpace = state.nextNonSpaceIndex
            val line = state.line
            val trySequence = line.subSequence(nextNonSpace, line.length)

            val block = FILE_CONTAINER_OPENING_PATTERN.match(trySequence) {
                FileContainer(options).apply {
                    openingMarker = subSequenceOfGroup(1)
                    path = subSequenceOfGroup(2)
                    language = subSequenceOfGroup(3)
                }
            } ?: CODE_CONTAINER_OPENING_PATTERN.match(trySequence) {
                CodeContainer(options).apply {
                    openingMarker = subSequenceOfGroup(1)
                    path = subSequenceOfGroup(2)
                    section = subSequenceOfGroup(3)
                }
            } ?: return BlockStart.none()

            val parser = TabContainerParser(block)
            return BlockStart.of(parser).atIndex(line.length)
        }
    }

    companion object {
        private val FILE_CONTAINER_OPENING_PATTERN = Pattern.compile("^(===)\\s+((?:/[\\w.\\-]*)*)(?: (\\w+))?\\s*$")
        private val CODE_CONTAINER_OPENING_PATTERN = Pattern.compile("^(===)\\s+((?:\\w+\\.)+\\w+):(\\*|\\w+)\\s*$")
        private val CLOSING_PATTERN = Pattern.compile("^(=/=)\\s*$")
    }
}


class LanguageSpecificContentParser(private val block: LanguageSpecificContent): AbstractBlockParser() {
    override fun getBlock(): Block = block

    override fun isContainer(): Boolean = true
    override fun canContain(state: ParserState, blockParser: BlockParser, block: Block): Boolean
            = block !is LanguageSpecificContent

    override fun tryContinue(state: ParserState): BlockContinue? {
        val nextNonSpace = state.nextNonSpaceIndex
        val line = state.line

        if (state.indent <= 3 && nextNonSpace < line.length) {
            val trySequence = line.subSequence(nextNonSpace, line.length)

            return CLOSING_PATTERN.match(trySequence) {
                block.closingMarker = subSequenceOfGroup(1)
                BlockContinue.finished()
            } ?: BlockContinue.atIndex(nextNonSpace)
        }
        return BlockContinue.atIndex(nextNonSpace)
    }

    override fun closeBlock(state: ParserState) {
        block.setCharsFromContent()
    }

    object Factory: CustomBlockParserFactory {
        override fun apply(options: DataHolder): BlockParserFactory =
            ParserFactory(options)

        override fun getAfterDependents(): MutableSet<Class<*>>? = null
        override fun getBeforeDependents(): MutableSet<Class<*>>? = null

        override fun affectsGlobalScope(): Boolean = false
    }
    class ParserFactory(options: DataHolder) : AbstractBlockParserFactory(options) {
        override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
            if (state.indent >= 4 || matchedBlockParser.blockParser !is TabContainerParser)
                return BlockStart.none()

            val nextNonSpace = state.nextNonSpaceIndex
            val line = state.line
            val trySequence = line.subSequence(nextNonSpace, line.length)

            val block = OPENING_PATTERN.match(trySequence) {
                LanguageSpecificContent().apply {
                    openingMarker = subSequenceOfGroup(1)
                    languages = subSequenceOfGroup(2)
                }
            } ?: return BlockStart.none()

            val parser = LanguageSpecificContentParser(block)
            return BlockStart.of(parser).atIndex(line.length)
        }
    }

    companion object {
        private val OPENING_PATTERN = Pattern.compile("^(=-=)\\s+((?:\\w+, )*\\w+)\\s*$")
        private val CLOSING_PATTERN = Pattern.compile("^(=-=)\\s*$")
    }
}