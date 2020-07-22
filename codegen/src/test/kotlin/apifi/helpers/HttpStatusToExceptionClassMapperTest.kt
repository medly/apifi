package apifi.helpers

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HttpStatusToExceptionClassMapperTest : StringSpec() {

    init {
        "should return exception classes given a list of status" {
            HttpStatusToExceptionClassMapper().getExceptionClassFor(listOf(400, 500, 401)) shouldBe
                    listOf("BadRequestException", "InternalServerErrorException", "UnauthorizedException")
        }

        "should return class for internal server error exception if no specific exception class found for a status" {
            HttpStatusToExceptionClassMapper().getExceptionClassFor(listOf(405, 302, 507)).distinct() shouldBe
                    listOf("InternalServerErrorException")
        }

    }

}