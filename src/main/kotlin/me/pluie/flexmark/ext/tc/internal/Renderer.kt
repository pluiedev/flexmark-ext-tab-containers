package me.pluie.flexmark.ext.tc.internal

import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import me.pluie.flexmark.ext.tc.*

object TabContainerRenderer: NodeRenderer {
    override fun getNodeRenderingHandlers() = mutableSetOf(
        NodeRenderingHandler(CodeContainer::class.java, this::render),
        NodeRenderingHandler(FileContainer::class.java, this::render),
        NodeRenderingHandler(LanguageSpecificContent::class.java, this::render),
    )

    private fun render(node: TabContainer, context: NodeRendererContext, html: HtmlWriter) {
        val entries = mutableSetOf<Entry>().apply(node::resolveEntries)

        html.withClass("tab-container").div {
            if (node is CodeContainer) {
                withClass("tabs-header").div {
                    withClass("tabs").div {
                        new("ul") {
                            entries.forEach {
                                renderLink(it, html)
                            }
                        }
                    }
                }
            }

            pre {
                entries.forEach {
                    withClass("language-${it.lang.id} is-${it.lang.id}").new("code") {
                        text(it.content)
                    }
                }
            }

            context.renderChildren(node)
        }
    }
    private fun render(node: LanguageSpecificContent, context: NodeRendererContext, html: HtmlWriter) {
        val classes = node.languages.splitToSequence(", ")
            .joinToString(separator = " ") { "is-$it" }

        html.withClass(classes).div {
            context.renderChildren(node)
        }
    }

    private fun renderLink(entry: Entry, html: HtmlWriter) {
        html.withAttr().attr("for", "lang-switch-${entry.lang.id}").new("label") {
            new("li") {
                withAttr().attr("href", entry.link).new("a") {
                    text(entry.lang.displayName!!)
                }
            }
        }
    }
}

fun HtmlWriter.withClass(value: String): HtmlWriter = withAttr().attr("class", value)

inline fun HtmlWriter.new(name: String, action: HtmlWriter.() -> Unit): HtmlWriter = run {
    tag(name).action()
    closeTag(name)
}
inline fun HtmlWriter.div(action: HtmlWriter.() -> Unit): HtmlWriter =
    new("div", action)
inline fun HtmlWriter.pre(action: HtmlWriter.() -> Unit): HtmlWriter =
    new("pre") {
        openPre()
        action()
        closePre()
    }