package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.data.SharedDataKeys
import org.junit.jupiter.api.Assertions
import kotlin.io.path.Path
import kotlin.test.assertIs

internal val TEST_DATA = Path("src/test/resources/testData")
internal val DATA = MutableDataSet().apply {
    set(SharedDataKeys.EXTENSIONS, listOf(TabContainerExtension))
    set(TabContainerExtension.BASE_DIR, TEST_DATA)
}.toImmutable()

internal val PARSER = Parser.builder(DATA).build()

class AssertChildrenScope(val children: Iterator<Node>) {
    inline fun <reified T> nextIs(test: T.() -> Unit) {
        assertIs<T>(children.next()).apply(test)
    }
    inline fun nextParagraphIs(text: () -> String) {
        nextIs<Paragraph> {
            Assertions.assertEquals(text(), contentChars.trim().toString())
        }
    }
}

inline fun Node.assertChildren(test: AssertChildrenScope.() -> Unit) {
    AssertChildrenScope(childIterator).test()
}