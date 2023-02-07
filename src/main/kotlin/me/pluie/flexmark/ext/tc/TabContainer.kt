package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence
import java.lang.StringBuilder
import kotlin.io.path.readText

sealed class TabContainer(options: DataHolder): Block() {
    protected val settings = TabContainerExtension.Settings(options)

    var openingMarker: BasedSequence = BasedSequence.NULL
    var path: BasedSequence = BasedSequence.NULL
    var closingMarker: BasedSequence = BasedSequence.NULL

    abstract fun resolveEntries(out: MutableSet<Entry>)

    override fun getSegments() = arrayOf(openingMarker, path, closingMarker)

    override fun getAstExtra(out: StringBuilder) {
        segmentSpanChars(out, openingMarker, "openingMarker")
        segmentSpanChars(out, path, "path")
        segmentSpanChars(out, closingMarker, "closingMarker")
    }

    override fun toString() = toAstString(true)
}

class FileContainer(options: DataHolder): TabContainer(options) {
    var language: BasedSequence = BasedSequence.NULL

    override fun getSegments() = arrayOf(openingMarker, path, language, closingMarker)

    override fun getAstExtra(out: StringBuilder) {
        super.getAstExtra(out)
        segmentSpanChars(out, language, "language")
    }


    override fun resolveEntries(out: MutableSet<Entry>) {
        val path = settings.baseDir.resolve(path.substring(1))
        val text = path.readText()

        val relativePath = settings.rootDir.relativize(path)
        val link = settings.linkRoot.resolve(relativePath.toUri()).toString()

        out += Entry(Language(language.toString()), link, text)
    }
}
class CodeContainer(options: DataHolder): TabContainer(options) {
    var section: BasedSequence = BasedSequence.NULL

    override fun getSegments() = arrayOf(openingMarker, path, section, closingMarker)

    override fun getAstExtra(out: StringBuilder) {
        super.getAstExtra(out)
        segmentSpanChars(out, section, "section")
    }

    override fun resolveEntries(out: MutableSet<Entry>) {
        settings.languages.mapTo(out) {
            val codeBaseDir = settings.codeBaseDirResolver(it)
            val relPath = codeBaseDir.resolve(path.replace(".", "/").toString() + ".${it.extension}")
            val path = settings.baseDir.resolve(relPath)
            val text = path.readText()

            val section = it.extractSection(text, section.toString())

            val link = buildString {
                val relativePath = settings.rootDir.relativize(path)
                append(settings.linkRoot.resolve(relativePath.toUri()))

                if (section != null) {
                    append("#L")
                    append(section.lineRange.first)
                    if (section.lineRange.first != section.lineRange.last) {
                        append('-')
                        append(section.lineRange.last)
                    }
                }
            }

            Entry(it, link, section?.content ?: text)
        }
    }
}
