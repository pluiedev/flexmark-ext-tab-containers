package me.pluie.flexmark.ext.tc

import com.vladsch.flexmark.ast.Heading
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class ParserTest {

    @Test
    fun simpleFileContainer() {
        val doc = PARSER.parse("""
            === /src/main/resources/assets/tutorial/lang/en_us.json json
            =/=
        """.trimIndent())

        doc.assertChildren {
            nextIs<FileContainer> {
                assertEquals("===", openingMarker.toString())
                assertEquals("/src/main/resources/assets/tutorial/lang/en_us.json", path.toString())
                assertEquals("json", language.toString())
                assertEquals("=/=", closingMarker.toString())
            }
        }
    }

    @Test
    fun simpleCodeContainer() {
        val doc = PARSER.parse("""
            === com.example.test.Fictional:some_section
            =/=
        """.trimIndent())

        doc.assertChildren {
            nextIs<CodeContainer> {
                assertEquals("===", openingMarker.toString())
                assertEquals("com.example.test.Fictional", path.toString())
                assertEquals("some_section", section.toString())
                assertEquals("=/=", closingMarker.toString())
            }
        }
    }

    @Test
    fun withSurroundingText() {
        val doc = PARSER.parse("""
            # This is some text
            
            Some other text. This is a paragraph.
            More text.
            
            === com.example.test.Fictional:some_section
            =/=
            
            === /src/main/resources/assets/tutorial/lang/en_us.json json
            =/=
            
            More surrounding text...
        """.trimIndent())

        doc.assertChildren {
            nextIs<Heading> {
                assertEquals(1, level)
                assertEquals("This is some text", text.toString())
            }
            nextParagraphIs { "Some other text. This is a paragraph.\nMore text." }
            nextIs<CodeContainer> {
                assertEquals("===", openingMarker.toString())
                assertEquals("com.example.test.Fictional", path.toString())
                assertEquals("some_section", section.toString())
                assertEquals("=/=", closingMarker.toString())
            }
            nextIs<FileContainer> {
                assertEquals("===", openingMarker.toString())
                assertEquals("/src/main/resources/assets/tutorial/lang/en_us.json", path.toString())
                assertEquals("json", language.toString())
                assertEquals("=/=", closingMarker.toString())
            }
            nextParagraphIs { "More surrounding text..." }
        }
    }

    @Test
    fun withLanguageSpecificContent() {
        val doc = PARSER.parse("""
            === com.example.test.Fictional:some_section
            =-= haha
            ¡Hola!
            
            =-=
            =-= hoho, popo
            Hey!
            
            =-=
            =/=
        """.trimIndent())

        doc.assertChildren {
            nextIs<CodeContainer> {
                assertEquals("===", openingMarker.toString())
                assertEquals("com.example.test.Fictional", path.toString())
                assertEquals("some_section", section.toString())
                assertEquals("=/=", closingMarker.toString())

                assertChildren {
                    nextIs<LanguageSpecificContent> {
                        assertEquals("=-=", openingMarker.toString())
                        assertEquals("haha", languages.toString())
                        assertEquals("=-=", closingMarker.toString())

                        assertChildren {
                            nextParagraphIs { "¡Hola!" }
                        }
                    }
                    nextIs<LanguageSpecificContent> {
                        assertEquals("=-=", openingMarker.toString())
                        assertEquals("hoho, popo", languages.toString())
                        assertEquals("=-=", closingMarker.toString())

                        assertChildren {
                            nextParagraphIs { "Hey!" }
                        }
                    }
                }
            }
        }
    }
}