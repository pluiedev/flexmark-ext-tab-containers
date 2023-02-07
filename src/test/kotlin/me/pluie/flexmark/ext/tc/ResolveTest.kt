package me.pluie.flexmark.ext.tc

import org.junit.jupiter.api.Test

import kotlin.io.path.readText
import kotlin.test.assertEquals

class ResolveTest {

    @Test
    fun simpleFileContainer() {
        val doc = PARSER.parse("""
            === /haha.json json
            =/=
        """.trimIndent())
        doc.assertChildren {
            nextIs<FileContainer> {
                val entries = mutableSetOf<Entry>().apply(this::resolveEntries)
                assertEquals(TEST_DATA.resolve("haha.json").readText(), entries.first().content)
            }
        }
    }
    @Test
    fun simpleCodeContainer() {
        val doc = PARSER.parse("""
            === me.pluie.Example:hello_world
            =/=
        """.trimIndent())
        doc.assertChildren {
            nextIs<CodeContainer> {
                val entries = mutableSetOf<Entry>().apply(this::resolveEntries)
                entries.forEach {
                    when (it.lang.id) {
                        "java" -> assertEquals("""
                            public static void main(String[] args) {
                                System.out.println("Hello world!")
                            }
                        """.trimIndent(), it.content)
                        "kotlin" -> assertEquals("""
                            fun main() {
                                println("Hello world!")
                            }
                        """.trimIndent(), it.content)
                        "scala" -> assertEquals("@main def hello() = println(\"Hello world!\")", it.content)
                        "groovy" -> assertEquals("println(\"Hello world!\")", it.content)
                    }
                }
            }
        }
    }
}