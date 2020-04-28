package apifi.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils
import kotlin.test.assertEquals

class ResponseBodyParserTest : DescribeSpec({

    describe("Response Body Parser") {
        it("should parse response body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-responses-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val response = ResponseBodyParser.parse(openApi.paths["/pets/{petId}"]?.get?.responses)
            response shouldBe listOf("PetResponse", "Error")
        }
    }
})