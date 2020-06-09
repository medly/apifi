package apifi.codegen

import apifi.helpers.toKotlinPoetType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

class RequestBodyBuilderTest : DescribeSpec( {

    describe("Request Body Builder") {
        it("should generate request body type") {
            val requestBody = ParameterSpec.builder("body", "Pet".toKotlinPoetType(mapOf("Pet" to "models.Pet")))
                .addAnnotation(ClassName("io.micronaut.http.annotation", "Body"))
                .build()
            requestBody.toString() shouldBe "@io.micronaut.http.annotation.Body body: models.Pet"
        }
    }

})