package me.pluie.flexmark.ext.tc.internal

import com.vladsch.flexmark.formatter.MarkdownWriter
import com.vladsch.flexmark.formatter.NodeFormatter
import com.vladsch.flexmark.formatter.NodeFormatterContext
import com.vladsch.flexmark.formatter.NodeFormattingHandler
import me.pluie.flexmark.ext.tc.CodeContainer
import me.pluie.flexmark.ext.tc.FileContainer
import me.pluie.flexmark.ext.tc.TabContainer

object TabContainerFormatter: NodeFormatter {
    override fun getNodeFormattingHandlers() = mutableSetOf(
        NodeFormattingHandler(CodeContainer::class.java, this::render),
        NodeFormattingHandler(FileContainer::class.java, this::render),
    )

    private fun render(node: TabContainer, context: NodeFormatterContext, md: MarkdownWriter) {
        md.blankLine()
        md.append(node.openingMarker).append(' ')
        md.appendNonTranslating(node.path)
        when (node) {
            is FileContainer -> if (node.language.isNotNull) md.append(' ').appendNonTranslating(node.language)
            is CodeContainer -> if (node.section.isNotNull) md.append(':').appendNonTranslating(node.section)
        }
        md.line()
        context.renderChildren(node)
        md.blankLine()
        md.append(node.closingMarker)
    }

    override fun getNodeClasses(): MutableSet<Class<*>>? = null
}