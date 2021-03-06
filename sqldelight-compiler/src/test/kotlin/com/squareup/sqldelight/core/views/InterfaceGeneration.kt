package com.squareup.sqldelight.core.views

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.core.compiler.SqlDelightCompiler
import com.squareup.sqldelight.test.util.FixtureCompiler
import com.squareup.sqldelight.test.util.withInvariantLineSeparators
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class InterfaceGeneration {
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Test fun onlyTableType() {
    checkFixtureCompiles("only-table-type")
  }

  @Test fun requiresAdapter() {
    checkFixtureCompiles("requires-adapter")
  }

  @Test fun `view with exposed booleans through union`() {
    val result = FixtureCompiler.compileSql("""
      |CREATE TABLE test (
      |  val INTEGER AS Boolean NOT NULL
      |);
      |
      |CREATE VIEW someView AS
      |SELECT val, val
      |FROM test
      |UNION
      |SELECT 0, 0
      |FROM test;
      |""".trimMargin(), temporaryFolder)

    assertThat(result.errors).isEmpty()
    val generatedInterface = result.compilerOutput.get(
        File(result.outputDirectory, "com/example/SomeView.kt")
    )
    assertThat(generatedInterface).isNotNull()
    assertThat(generatedInterface.toString()).isEqualTo("""
      |package com.example
      |
      |import kotlin.Boolean
      |import kotlin.String
      |
      |interface SomeView {
      |  val val_: Boolean
      |
      |  val val__: Boolean
      |
      |  data class Impl(
      |    override val val_: Boolean,
      |    override val val__: Boolean
      |  ) : SomeView {
      |    override fun toString(): String = ""${'"'}
      |    |SomeView.Impl [
      |    |  val_: ${"$"}val_
      |    |  val__: ${"$"}val__
      |    |]
      |    ""${'"'}.trimMargin()
      |  }
      |}
      |""".trimMargin())
  }

  @Test fun `view with exposed booleans through union of separate tables`() {
    val result = FixtureCompiler.compileSql("""
      |CREATE TABLE test (
      |  val INTEGER AS Boolean NOT NULL
      |);
      |
      |CREATE TABLE another_test (
      |  val INTEGER AS Boolean NOT NULL
      |);
      |
      |CREATE VIEW someView AS
      |SELECT val, val
      |FROM test
      |UNION
      |SELECT val, val
      |FROM another_test;
      |""".trimMargin(), temporaryFolder)

    assertThat(result.errors).isEmpty()
    val generatedInterface = result.compilerOutput.get(
        File(result.outputDirectory, "com/example/SomeView.kt")
    )
    assertThat(generatedInterface).isNotNull()
    assertThat(generatedInterface.toString()).isEqualTo("""
      |package com.example
      |
      |import kotlin.Boolean
      |import kotlin.String
      |
      |interface SomeView {
      |  val val_: Boolean
      |
      |  val val__: Boolean
      |
      |  data class Impl(
      |    override val val_: Boolean,
      |    override val val__: Boolean
      |  ) : SomeView {
      |    override fun toString(): String = ""${'"'}
      |    |SomeView.Impl [
      |    |  val_: ${"$"}val_
      |    |  val__: ${"$"}val__
      |    |]
      |    ""${'"'}.trimMargin()
      |  }
      |}
      |""".trimMargin())
  }

  @Test fun `kotlin array types are printed properly`() {
    val result = FixtureCompiler.compileSql("""
      |CREATE TABLE test (
      |  arrayValue BLOB AS kotlin.Array<kotlin.Int> NOT NULL,
      |  booleanArrayValue BLOB AS kotlin.BooleanArray NOT NULL,
      |  byteArrayValue BLOB AS kotlin.ByteArray NOT NULL,
      |  charArrayValue BLOB AS kotlin.CharArray NOT NULL,
      |  doubleArrayValue BLOB AS kotlin.DoubleArray NOT NULL,
      |  floatArrayValue BLOB AS kotlin.FloatArray NOT NULL,
      |  intArrayValue BLOB AS kotlin.IntArray NOT NULL,
      |  longArrayValue BLOB AS kotlin.LongArray NOT NULL,
      |  shortArrayValue BLOB AS kotlin.ShortArray NOT NULL
      |);
      |
      |CREATE VIEW someView AS
      |SELECT *, 1
      |FROM test;
      |""".trimMargin(), temporaryFolder)

    assertThat(result.errors).isEmpty()
    val generatedInterface = result.compilerOutput.get(
        File(result.outputDirectory, "com/example/SomeView.kt")
    )
    assertThat(generatedInterface).isNotNull()
    assertThat(generatedInterface.toString()).isEqualTo("""
      |package com.example
      |
      |import kotlin.Array
      |import kotlin.BooleanArray
      |import kotlin.ByteArray
      |import kotlin.CharArray
      |import kotlin.DoubleArray
      |import kotlin.FloatArray
      |import kotlin.Int
      |import kotlin.IntArray
      |import kotlin.Long
      |import kotlin.LongArray
      |import kotlin.ShortArray
      |import kotlin.String
      |import kotlin.collections.contentToString
      |
      |interface SomeView {
      |  val arrayValue: Array<Int>
      |
      |  val booleanArrayValue: BooleanArray
      |
      |  val byteArrayValue: ByteArray
      |
      |  val charArrayValue: CharArray
      |
      |  val doubleArrayValue: DoubleArray
      |
      |  val floatArrayValue: FloatArray
      |
      |  val intArrayValue: IntArray
      |
      |  val longArrayValue: LongArray
      |
      |  val shortArrayValue: ShortArray
      |
      |  val expr: Long
      |
      |  data class Impl(
      |    override val arrayValue: Array<Int>,
      |    override val booleanArrayValue: BooleanArray,
      |    override val byteArrayValue: ByteArray,
      |    override val charArrayValue: CharArray,
      |    override val doubleArrayValue: DoubleArray,
      |    override val floatArrayValue: FloatArray,
      |    override val intArrayValue: IntArray,
      |    override val longArrayValue: LongArray,
      |    override val shortArrayValue: ShortArray,
      |    override val expr: Long
      |  ) : SomeView {
      |    override fun toString(): String = ""${'"'}
      |    |SomeView.Impl [
      |    |  arrayValue: ${'$'}{arrayValue.contentToString()}
      |    |  booleanArrayValue: ${'$'}{booleanArrayValue.contentToString()}
      |    |  byteArrayValue: ${'$'}{byteArrayValue.contentToString()}
      |    |  charArrayValue: ${'$'}{charArrayValue.contentToString()}
      |    |  doubleArrayValue: ${'$'}{doubleArrayValue.contentToString()}
      |    |  floatArrayValue: ${'$'}{floatArrayValue.contentToString()}
      |    |  intArrayValue: ${'$'}{intArrayValue.contentToString()}
      |    |  longArrayValue: ${'$'}{longArrayValue.contentToString()}
      |    |  shortArrayValue: ${'$'}{shortArrayValue.contentToString()}
      |    |  expr: ${'$'}expr
      |    |]
      |    ""${'"'}.trimMargin()
      |  }
      |}
      |""".trimMargin())
  }

  private fun checkFixtureCompiles(fixtureRoot: String) {
    val result = FixtureCompiler.compileFixture(
        "src/test/view-interface-fixtures/$fixtureRoot",
        SqlDelightCompiler::writeViewInterfaces,
        false)
    for ((expectedFile, actualOutput) in result.compilerOutput) {
      assertThat(expectedFile.exists()).named("No file with name $expectedFile").isTrue()
      assertThat(expectedFile.readText().withInvariantLineSeparators())
          .named(expectedFile.name)
          .isEqualTo(actualOutput.toString())
    }
  }
}
