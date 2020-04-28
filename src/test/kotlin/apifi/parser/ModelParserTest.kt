package apifi.parser

import apifi.parser.models.Model
import apifi.parser.models.Property
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class ModelParserTest: DescribeSpec({

    describe("Model Parser") {
        it("should parse models from spec with separate schemas") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val models = openApi.components.schemas.map { ModelParser.modelFromSchema(it.key, it.value) }
            models.size shouldBe 2
            models[0] shouldBe Model("Pet", listOf(
                    Property("id", "kotlin.Long", false),
                    Property("name", "kotlin.String", false),
                    Property("tag", "kotlin.String", true)
            ))
            models[1] shouldBe Model("Error", listOf(
                    Property("code", "kotlin.Int", false),
                    Property("message", "kotlin.String", false)
            ))
        }
    }
})