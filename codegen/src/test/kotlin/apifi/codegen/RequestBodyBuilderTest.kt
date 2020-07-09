package apifi.codegen

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class RequestBodyBuilderTest : DescribeSpec( {

    describe("Request Body Builder") {
        it("should generate request body type") {
            val requestBody = RequestBodyBuilder.build("Pet", mapOf("Pet" to "models.Pet"))
            requestBody.toString() shouldBe "@io.micronaut.http.annotation.Body body: models.Pet"
        }
    }

})