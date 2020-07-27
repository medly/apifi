package apifi.parser

import apifi.codegen.micronautMultipartFileUploadPackage
import apifi.models.Model
import apifi.models.Property
import apifi.parser.models.Request
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class RequestBodyParserTest : DescribeSpec({

    describe("Request Body Parser") {
        it("should parse request body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-separate-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val request = RequestBodyParser.parse(openApi.paths["/pets"]?.post?.requestBody, "showById")
            request?.result shouldBe Request("kotlin.Array<Pet>", listOf("application/json"))
            request?.models shouldBe emptyList()
        }

        it("should parse request body with inline body") {
            val file = FileUtils.getFile("src", "test-res", "parser", "models", "with-inline-request-response-schema.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val request = RequestBodyParser.parse(openApi.paths["/pets"]?.post?.requestBody, "showById")
            request?.result shouldBe Request("kotlin.Array<ShowByIdRequest>", listOf("application/json"))
            request?.models shouldBe listOf(
                    Model("ShowByIdRequest", listOf(
                            Property("id", "kotlin.Long", false),
                            Property("name", "kotlin.String", false),
                            Property("tags", "kotlin.Array<kotlin.String>", true)
                    ))
            )
        }

        it("should generate request for multipart content type") {
            val file = FileUtils.getFile("src", "test-res", "parser", "request", "with-multipart-content-type.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val request = RequestBodyParser.parse(openApi.paths["/pet/{id}/uploadDoc"]?.post?.requestBody, "uploadDocument")
            request?.result shouldBe Request(micronautMultipartFileUploadPackage, listOf("multipart/form-data"))
            request?.models shouldBe emptyList()
        }
    }
})