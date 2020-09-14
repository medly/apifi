package apifi.parser

import apifi.models.Model
import apifi.models.Property
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class ModelParserTest : DescribeSpec({

    describe("Model Parser") {
        it("should parse models from spec with separate schemas") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val models = openApi.components.schemas.map { ModelParser.modelsFromSchema(it.key, it.value) }
            models.size shouldBe 2
            models[0] shouldBe listOf(
                Model("Pet", listOf(
                    Property("id", "kotlin.Long", false),
                    Property("name", "kotlin.String", false),
                    Property("tags", "kotlin.Array<kotlin.String>", true),
                    Property("children", "kotlin.Array<Children>", true)
                )),
                Model("Children", listOf(
                    Property("name", "kotlin.String", true),
                    Property("gender", "Gender", true)
                )),
                Model("Gender", emptyList(), listOf("MALE", "FEMALE", "OTHERS"))
            )
            models[1] shouldBe listOf(Model("Error", listOf(
                Property("code", "kotlin.Int", false),
                Property("message", "kotlin.String", false)
            )))
        }

        it("should parse model from schema which refers to another model schema") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-cross-reference-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val models = openApi.components.schemas.map { ModelParser.modelsFromSchema(it.key, it.value) }
            models.size shouldBe 3
            models[0] shouldBe listOf(
                Model("Pet", listOf(
                    Property("id", "kotlin.Long", false),
                    Property("name", "kotlin.String", false),
                    Property("tag", "kotlin.String", true),
                    Property("child", "Child", true),
                    Property("foodChoices", "kotlin.Array<FoodChoice>", true)
                )
                ))
            models[1] shouldBe listOf(Model("Child", listOf(
                Property("name", "kotlin.String", false)
            )))
            models[2] shouldBe listOf(Model("FoodChoice", listOf(
                Property("name", "kotlin.String", false)
            )))
        }
    }
})