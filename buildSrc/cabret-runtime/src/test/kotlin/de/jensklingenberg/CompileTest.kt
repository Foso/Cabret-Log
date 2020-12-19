package de.jensklingenberg


import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.jensklingenberg.debuglog.MyIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.junit.Assert
import org.junit.Test

open class TestCommonComponentRegistrar() : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        IrGenerationExtension.registerExtension(project, MyIrGenerationExtension(messageCollector))


    }
}

annotation class TestAnnotation()


class CompilerPluginsTest {


    val kotlinSource2 = SourceFile.kotlin("KClass.kt", """
        @file:TestFile
        package de.jensklingenberg.testAnnotations

@TestFunction
fun highOrder() = "hi"

@TestTypeAlias
typealias Word = String

@TestAnnotationClass
annotation class  AnnotatedAnnotatedClass

         @TestClass
    class Annotated @TestConstructor constructor() {

    constructor(name: String) : this()

    @TestProperty
    @TestField
    lateinit var myProperty: @TestType String

    var jens: String
        @TestPropertyGetter get() {
           return "Hello"
        }
        @TestPropertySetter set(value) {

        }

    @TestFunction
    fun <@TestTypeParameter T> firstFunction() : String {
           @TestExpression return ""
    }


    fun thirdFunction(@TestValueParameter param: String) {
      @TestLocalVariable val localHallo :String
    }

}
    """)


    @Test
    fun `find class Annotation`() {

        var actualFoundName = ""


        compileKotlin()
        Assert.assertEquals("", actualFoundName)
    }


    private fun compileKotlin() {
        KotlinCompilation().apply {
            sources = listOf(kotlinSource2)


            // pass your own instance of a compiler plugin
            compilerPlugins = listOf(TestCommonComponentRegistrar())

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

}