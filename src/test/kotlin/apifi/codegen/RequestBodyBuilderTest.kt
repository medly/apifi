package apifi.codegen

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class RequestBodyBuilderTest : DescribeSpec( {

    describe("Request Body Builder") {
        it("should generate request body type") {
            val requestBody = RequestBodyBuilder.build("Pet", listOf("Pet" to "models.Pet"))
            requestBody.toString() shouldBe "@io.micronaut.http.annotation.Body body: models.Pet"
        }
    }

})