package apifi.codegen

import apifi.parser.models.Param
import apifi.parser.models.ParamType
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class HeaderBuilderTest : DescribeSpec({
    describe("HeaderBuilder") {
        it("should return correct ParameterSpec for mandatory header param") {
            val headerParam = Param("X-Foo", "String", true, ParamType.Path)
            HeaderBuilder.build(headerParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.Header(value = \"X-Foo\") xFoo: String"
        }

        it("should return correct ParameterSpec for optional header param") {
            val queryParam = Param("X-Foo", "String", false, ParamType.Path)
            HeaderBuilder.build(queryParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.Header(value = \"X-Foo\") xFoo: String?"
        }
    }
})