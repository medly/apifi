package apifi.codegen

import apifi.parser.models.Param
import apifi.parser.models.ParamType
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class PathVariableBuilderTest : DescribeSpec({
    describe("PathVariableBuilder") {
        it("should return correct ParameterSpec for mandatory path param") {
            val pathParam = Param("name", "String", true, ParamType.Path)
            PathVariableBuilder.build(pathParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable name: String"
        }

        it("should return correct ParameterSpec for optional path param") {
            val queryParam = Param("name", "String", false, ParamType.Path)
            PathVariableBuilder.build(queryParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.PathVariable name: String?"
        }
    }
})