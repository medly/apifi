package apifi.parser

import apifi.parser.models.Model
import apifi.parser.models.Property
import apifi.parser.models.Response
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class ResponseBodyParserTest : DescribeSpec({

    describe("Response Body Parser") {
        it("should parse response body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets/{petId}"]?.get?.responses, "showByPetId")
            response?.first shouldBe listOf(Response("200", "Pet"), Response("default", "Error"))
            response?.second shouldBe emptyList()
        }

        it("should parse response body with inline schema") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-inline-request-response-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets"]?.post?.responses, "showByPetId")
            response?.first shouldBe listOf(Response("default","ShowByPetIdResponse"))
            response?.second shouldBe listOf(
                    Model("ShowByPetIdResponse", listOf(
                            Property("code", "kotlin.Int", false),
                            Property("message", "kotlin.String", false)
                    ))
            )
        }
    }
})