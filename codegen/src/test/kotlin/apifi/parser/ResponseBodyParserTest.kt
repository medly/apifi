package apifi.parser

import apifi.models.Model
import apifi.models.Property
import apifi.parser.models.Response
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class ResponseBodyParserTest : DescribeSpec({

    describe("Response Body Parser") {
        it("should parse response body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets/{petId}"]?.get?.responses, "showByPetId")
            response?.result shouldBe listOf(Response("200", "Pet"), Response("default", "Error"))
            response?.models shouldBe emptyList()
        }

        it("should parse response body with inline schema") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-inline-request-response-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets"]?.post?.responses, "showByPetId")
            response?.result shouldBe listOf(Response("200", "kotlin.collections.List<ShowByPetIdResponse>"), Response("default", "ShowByPetIdResponse"))
            response?.models shouldBe listOf(
                    Model("ShowByPetIdResponse", listOf(
                            Property("id", "kotlin.Long", false),
                            Property("name", "kotlin.String", false),
                            Property("tags", "kotlin.collections.List<kotlin.String>", true))),
                    Model("ShowByPetIdResponse", listOf(
                            Property("code", "kotlin.Int", false),
                            Property("message", "kotlin.String", false)
                    ))
            )
        }

        it("should parse response types for non-200 responses also") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-non-200-responses.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets/{petId}"]?.get?.responses, "showByPetId")
            response?.result shouldBe listOf(Response("200", "PetResponse"), Response("400", "kotlin.String"), Response("default", "Error"))
            response?.models shouldBe emptyList()
        }
    }
})