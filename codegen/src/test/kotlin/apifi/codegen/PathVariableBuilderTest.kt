package apifi.codegen

import apifi.parser.models.Param
import apifi.parser.models.ParamType
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class PathVariableBuilderTest : DescribeSpec({
    describe("PathVariableBuilder") {
        it("should return correct ParameterSpec for mandatory path param") {
            val pathParam = Param("name", "String", true, ParamType.Path)
            PathVariableBuilder.build(pathParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable name: String"
        }

        it("should return correct ParameterSpec for hyphenated path param") {
            val pathParam = Param("param-name", "String", true, ParamType.Path)
            PathVariableBuilder.build(pathParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable(value = \"param-name\") paramName: String"
        }

        it("should return correct ParameterSpec for path param with custom model") {
            val pathParam = Param("pet", "Pet", true, ParamType.Path)
            PathVariableBuilder.build(pathParam, mapOf("Pet" to "com.api.Pet")).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable pet: com.api.Pet"
        }

        it("should return correct ParameterSpec for optional path param") {
            val pathParam = Param("name", "String", false, ParamType.Path)
            PathVariableBuilder.build(pathParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable name: String?"
        }
    }
})