package apifi.codegen

import apifi.parser.models.*
import com.squareup.kotlinpoet.TypeSpec
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class ModelFileBuilderTest : DescribeSpec({

    describe("Model Builder") {
        it("generate model file for single model") {
            val fileSpec = ModelFileBuilder.build(listOf(Model("Pet", listOf(
                    Property("id", "kotlin.Int", false),
                    Property("name", "kotlin.String", true)))),
                    "com.pets")

            fileSpec.name shouldBe "Models.kt"
            fileSpec.packageName shouldBe "com.pets.models"
            fileSpec.members.size shouldBe 1
            val petClass = fileSpec.members[0] as TypeSpec
            petClass.name shouldBe "Pet"

            fileSpec.toString().trimIndent() shouldBe "package com.pets.models\n" +
                    "\n" +
                    "import kotlin.Int\n" +
                    "import kotlin.String\n" +
                    "\n" +
                    "data class Pet(\n" +
                    "  val id: Int,\n" +
                    "  val name: String?\n" +
                    ")"

        }

        it("generate model file for multiple models") {
            val fileSpec = ModelFileBuilder.build(listOf(
                    Model("Pet", listOf(
                            Property("id", "kotlin.Int", false),
                            Property("name", "kotlin.String", true))),
                    Model("Error", listOf(
                            Property("message", "kotlin.String", false)))),
                    "com.pets")

            fileSpec.name shouldBe "Models.kt"
            fileSpec.packageName shouldBe "com.pets.models"
            fileSpec.members.size shouldBe 2
            (fileSpec.members[0] as TypeSpec).name shouldBe "Pet"
            (fileSpec.members[1] as TypeSpec).name shouldBe "Error"

            fileSpec.toString().trimIndent() shouldBe "package com.pets.models\n" +
                    "\n" +
                    "import kotlin.Int\n" +
                    "import kotlin.String\n" +
                    "\n" +
                    "data class Pet(\n" +
                    "  val id: Int,\n" +
                    "  val name: String?\n" +
                    ")\n" +
                    "\n" +
                    "data class Error(\n" +
                    "  val message: String\n" +
                    ")"
        }

        it("should generate model with parameterized properties") {
            val fileSpec = ModelFileBuilder.build(listOf(Model("Pet", listOf(
                    Property("id", "kotlin.Int", false),
                    Property("tags", "kotlin.Array<kotlin.String>", true)))),
                    "com.pets")

            fileSpec.toString().trimIndent() shouldBe "package com.pets.models\n" +
                    "\n" +
                    "import kotlin.Array\n" +
                    "import kotlin.Int\n" +
                    "import kotlin.String\n" +
                    "\n" +
                    "data class Pet(\n" +
                    "  val id: Int,\n" +
                    "  val tags: Array<String>?\n" +
                    ")"
        }

        it("should generate models with nested dependency") {
            val fileSpec = ModelFileBuilder.build(listOf(Model("Pet", listOf(
                    Property("id", "kotlin.Int", false),
                    Property("tags", "kotlin.Array<kotlin.String>", true),
                    Property("child", "Child", true))),
                    Model("Child", listOf(Property("name", "kotlin.String", false)))),
                    "com.pets")

            fileSpec.toString().trimIndent() shouldBe "package com.pets.models\n" +
                    "\n" +
                    "import kotlin.Array\n" +
                    "import kotlin.Int\n" +
                    "import kotlin.String\n" +
                    "\n" +
                    "data class Pet(\n" +
                    "  val id: Int,\n" +
                    "  val tags: Array<String>?,\n" +
                    "  val child: Child?\n" +
                    ")\n" +
                    "\n" +
                    "data class Child(\n" +
                    "  val name: String\n" +
                    ")"
        }
    }

})