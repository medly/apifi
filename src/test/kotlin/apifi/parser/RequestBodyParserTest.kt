package apifi.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class RequestBodyParserTest : DescribeSpec({

    describe("Request Body Parser") {
        it("should parse request body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val request = RequestBodyParser.parse(openApi.paths["/pets"]?.post?.requestBody)
            request shouldBe "kotlin.Array<Pet>"
        }
    }
})