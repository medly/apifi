package apifi.codegen

import apifi.parser.models.Operation
import apifi.parser.models.Response
import apifi.parser.models.SecurityDefinitionType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.responses.ApiResponses
import java.lang.IllegalStateException

class OperationExtensionTest : DescribeSpec({

    describe("operation return type") {
        it("when more than one 2xx response are specified, should throw exception") {
            val operation = Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), emptyList(), null, listOf(Response("201", "kotlin.String"), Response("200", "PetResponse")), SecurityDefinitionType.BASIC_AUTH)
            val exception = shouldThrow<IllegalStateException> { operation.returnType(testModelMapping()).toString() }
            exception.message shouldBe "Invalid responses defined for operation with identifier: getOpName. Has more than one 2xx responses defined"
        }
        it("when only default response is specified, should return type of default response") {
            val operation = Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), emptyList(), null, listOf(Response(ApiResponses.DEFAULT, "Error")), SecurityDefinitionType.BASIC_AUTH)
            operation.returnType(testModelMapping()).toString() shouldBe "io.micronaut.http.HttpResponse<models.Error>"
        }
        it("when 200 and default are specified, should return type of 200") {
            val operation = Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), emptyList(), null, listOf(Response("200", "Pet"), Response("default", "Error")), SecurityDefinitionType.BASIC_AUTH)
            operation.returnType(testModelMapping()).toString() shouldBe "io.micronaut.http.HttpResponse<models.Pet>"
        }
        it("when more than one responses are defined without default response") {
            val operation = Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), emptyList(), null, listOf(Response("200", "Pet"), Response("400", "Error")), SecurityDefinitionType.BASIC_AUTH)
            operation.returnType(testModelMapping()).toString() shouldBe "io.micronaut.http.HttpResponse<models.Pet>"
        }
    }
})


/*
if 200 and default - then default should have expected schema
if only default - don't restrict schema
if 200, other error codes - then other error codes should have expected schema
if 200, other error codes and default - then other error codes and default should have expected schema
 */
