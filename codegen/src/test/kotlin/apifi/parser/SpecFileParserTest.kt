package apifi.parser

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class SpecFileParserTest : DescribeSpec({

    describe("parse spec") {

        it("should parse models & security requirements") {
            val file = FileUtils.getFile("src", "test-res", "parser", "securityschemes", "with-basic-auth-security-scheme.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = OpenApiSpecReader().read(openApi)
            spec.securityRequirements shouldBe listOf("httpBasic")
        }

        it("should not throw errors when no security scheme present") {
            val file = FileUtils.getFile("src", "test-res", "parser", "params", "with-query-params.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = OpenApiSpecReader().read(openApi)
            spec.securityRequirements shouldBe emptyList()
            spec.models shouldBe emptyList()
        }
    }

})
