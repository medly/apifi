package apifi.codegen

import apifi.models.Param
import apifi.models.ParamType
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class QueryParamBuilderTest : DescribeSpec({
    describe("QueryParamBuilder") {
        it("should return correct ParameterSpec for mandatory query param") {
            val queryParam = Param("name", "String", true, ParamType.Query)
            QueryParamBuilder.build(queryParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue name: String"
        }

        it("should return correct ParameterSpec for optional query param") {
            val queryParam = Param("name", "String", false, ParamType.Query)
            QueryParamBuilder.build(queryParam).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue name: String?"
        }
    }

})
