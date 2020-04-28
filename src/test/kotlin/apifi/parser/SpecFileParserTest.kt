package apifi.parser

import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class SpecFileParserTest : DescribeSpec({

    describe("parse security schemes") {
        it("should not throw errors when no security scheme present") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-query-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = SpecFileParser.parse(openApi)
            spec.securityDefinitions shouldBe null
        }
        it("should parse a security scheme specified in open api") {
            val file = FileUtils.getFile("src", "test-res", "parser", "securityschemes", "with-basic-auth-security-scheme.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = SpecFileParser.parse(openApi)
            spec.securityDefinitions!!.size shouldBe 1
            spec.securityDefinitions!![0] shouldBe SecurityDefinition("httpBasic", SecurityDefinitionType.BASIC_AUTH)
        }
        it("should throw exception when security scheme other than http basic is mentioned") {
            val file = FileUtils.getFile("src", "test-res", "parser", "securityschemes", "with-non-basic-auth-security-scheme.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val exception = shouldThrow<IllegalStateException> { SpecFileParser.parse(openApi) }
            exception.message shouldBe "Security scheme not supported yet"
        }
    }

})