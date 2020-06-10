package apifi.codegen

import apifi.parser.SpecFileParser
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.FileUtils

class CodeGeneratorTest : DescribeSpec({

    describe("Code Generator") {
        it("should generate files according to spec") {
            val file = FileUtils.getFile("src", "test-res", "codegen", "all-paths.yml").readText().trimIndent()
            val openApi = OpenAPIV3Parser().readContents(file).openAPI
            val spec = SpecFileParser.parse(openApi)
            val fileSpecs = CodeGenerator.generate(spec, "com.pets")
            fileSpecs.size shouldBe 4

            val expectedPetApi = FileUtils.getFile("src", "test-res", "codegen", "expected-pet-api").readText()
            val expectedStoreApi = FileUtils.getFile("src", "test-res", "codegen", "expected-store-api").readText()
            val expectedModels = FileUtils.getFile("src", "test-res", "codegen", "expected-models").readText()
            val expectedResponseModel = FileUtils.getFile("src", "test-res", "codegen", "expected-response-model").readText()
            fileSpecs[0].toString() shouldBe expectedPetApi
            fileSpecs[1].toString() shouldBe expectedStoreApi
            fileSpecs[2].toString() shouldBe expectedModels
            fileSpecs[3].toString() shouldBe expectedResponseModel
        }
    }
}
)