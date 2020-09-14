package apifi.codegen

import apifi.parser.models.Param
import apifi.parser.models.ParamType
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class QueryParamBuilderTest : DescribeSpec({
    describe("QueryParamBuilder") {
        it("should return correct ParameterSpec for mandatory query param") {
            val queryParam = Param("name", "String", true, ParamType.Query)
            QueryParamBuilder.build(queryParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue name: String"
        }

        it("should return correct ParameterSpec for hyphenated query param") {
            val queryParam = Param("query-name", "String", true, ParamType.Query)
            QueryParamBuilder.build(queryParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue(value = \"query-name\") queryName: String"
        }

        it("should return correct ParameterSpec for query param with custom model") {
            val queryParam = Param("pet", "Pet", true, ParamType.Query)
            QueryParamBuilder.build(queryParam, mapOf("Pet" to "com.api.Pet")).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue pet: com.api.Pet"
        }

        it("should return correct ParameterSpec for optional query param") {
            val queryParam = Param("name", "String", false, ParamType.Query)
            QueryParamBuilder.build(queryParam, emptyMap()).toString().trimIndent() shouldBe "@io.micronaut.http.annotation.QueryValue name: String?"
        }
    }

})
