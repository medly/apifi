package apifi.codegen

import apifi.codegen.exceptions.Non200ResponseHandler
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class Non200ResponseHandlerTest : StringSpec() {

    init {
        "should return name of exception classes given a list of status" {
            Non200ResponseHandler.getExceptionClassFor(listOf(400, 500, 401)) shouldBe
                    listOf("BadRequestException", "InternalServerErrorException", "UnauthorizedException")
        }

        "should return class name for internal server error exception if no specific exception class found for a status" {
            Non200ResponseHandler.getExceptionClassFor(listOf(405, 302, 507)).distinct() shouldBe
                    listOf("InternalServerErrorException")
        }

    }

}