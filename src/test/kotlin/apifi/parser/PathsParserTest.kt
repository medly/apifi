package apifi.parser

import apifi.parser.models.Param
import apifi.parser.models.ParamType
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class PathsParserTest : DescribeSpec({

    describe("params") {
        it("with no params") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-no-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val path = PathsParser.parse(openApi.paths)[0]
            path.url shouldBe "/pets"
            path.operations!![0].params shouldBe null
            path.operations!![0].type shouldBe HttpMethod.GET
        }
        it("with query params") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-query-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val path = PathsParser.parse(openApi.paths)[0]
            path.url shouldBe "/pets"
            path.operations!![0].type shouldBe HttpMethod.POST
            path.operations!![0].params!![0] shouldBe Param("limit", "kotlin.Int", false, ParamType.Query)
        }
        it("with path params") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-path-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val path = PathsParser.parse(openApi.paths)[0]
            path.url shouldBe "/pets/{petId}"
            path.operations!![0].type shouldBe HttpMethod.GET
            path.operations!![0].params!![0] shouldBe Param("petId", "kotlin.String", true, ParamType.Path)
        }
        it("with headers") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-header-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val path = PathsParser.parse(openApi.paths)[0]
            path.url shouldBe "/pets"
            path.operations!![0].type shouldBe HttpMethod.POST
            path.operations!![0].params!![0] shouldBe Param("x-header", "kotlin.String", true, ParamType.Header)
        }
    }
})