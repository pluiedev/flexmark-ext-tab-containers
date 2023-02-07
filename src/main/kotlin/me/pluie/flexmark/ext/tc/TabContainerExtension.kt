package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.formatter.Formatter
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.MutableDataHolder
import me.pluie.flexmark.ext.tc.internal.LanguageSpecificContentParser
import me.pluie.flexmark.ext.tc.internal.TabContainerFormatter
import me.pluie.flexmark.ext.tc.internal.TabContainerParser
import me.pluie.flexmark.ext.tc.internal.TabContainerRenderer
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

object TabContainerExtension: Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension, Formatter.FormatterExtension {
    val LANGUAGES = DataKey("TAB_CONTAINER.LANGUAGES", setOf(
        Language("java", "Java"),
        Language("kotlin", "Kotlin", "kt"),
        Language("scala", "Scala 3"),
        Language("groovy", "Groovy"),
    ))
    val LINK_ROOT = DataKey("TAB_CONTAINER.LINK_ROOT", URI(""))
    val BASE_DIR = DataKey("TAB_CONTAINER.BASE_DIR", Path(""))
    val CODE_BASE_DIR_RESOLVER = DataKey("TAB_CONTAINER.CODE_BASE_DIR_RESOLVER") { lang: Language ->
        Path("src/main/${lang.id}")
    }
    val ROOT_DIR = DataKey("TAB_CONTAINER.ROOT_DIR", BASE_DIR)

    override fun parserOptions(options: MutableDataHolder) {}

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder
            .customBlockParserFactory(TabContainerParser.Factory)
            .customBlockParserFactory(LanguageSpecificContentParser.Factory)
    }

    override fun rendererOptions(options: MutableDataHolder) {}

    override fun extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String) {
        htmlRendererBuilder
            .nodeRendererFactory { TabContainerRenderer }
    }
    override fun extend(formatterBuilder: Formatter.Builder) {
        formatterBuilder
            .nodeFormatterFactory { TabContainerFormatter }
    }

    data class Settings(
        val languages: Set<Language>,
        val linkRoot: URI,
        val baseDir: Path,
        val codeBaseDirResolver: (Language) -> Path,
        val rootDir: Path,
    ) {
        constructor(options: DataHolder): this(
            LANGUAGES.get(options),
            LINK_ROOT.get(options),
            BASE_DIR.get(options),
            CODE_BASE_DIR_RESOLVER.get(options),
            ROOT_DIR.get(options),
        )
    }
}