package apifi.codegen

import apifi.parser.SpecFileParser
import io.kotest.matchers.collections.shouldNotContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class CodeGeneratorTest : DescribeSpec({

    describe("Code Generator") {
        it("should generate files according to spec") {
            val file = FileUtils.getFile("src", "test-res", "codegen", "all-paths.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = SpecFileParser.parse(openApi)
            val fileSpecs = CodeGenerator.generate(spec, "com.pets")
            fileSpecs.size shouldBe 8

            val expectedPetApi = FileUtils.getFile("src", "test-res", "codegen", "expected-pet-api").readText()
            val expectedStoreApi = FileUtils.getFile("src", "test-res", "codegen", "expected-store-api").readText()
            val expectedModels = FileUtils.getFile("src", "test-res", "codegen", "expected-models").readText()
            fileSpecs[0].toString() shouldBe expectedPetApi
            fileSpecs[1].toString() shouldBe expectedStoreApi
            fileSpecs[2].toString() shouldBe expectedModels
            fileSpecs.map { it.toString() } shouldNotContainInOrder  listOf("class BadRequestException", "class UnauthorizedException", "class ForbiddenException", "class NotFoundException", "class InternalServerException")
        }
    }
}
)