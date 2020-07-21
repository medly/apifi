package apifi.micronaut.exceptions

import kotlin.reflect.KClass

class HttpStatusToExceptionClassMapper {

    private val allExceptionDetailsHolder = listOf(
            ExceptionDetailsHolder(400, BadRequestException::class),
            ExceptionDetailsHolder(401, UnauthorizedException::class),
            ExceptionDetailsHolder(403, ForbiddenException::class),
            ExceptionDetailsHolder(404, NotFoundException::class),
            ExceptionDetailsHolder(500, InternalServerErrorException::class)
    )

    fun getExceptionClassFor(statuses: List<Int>) = statuses.map { status -> allExceptionDetailsHolder.find { it.status == status }?.exceptionClass ?: InternalServerErrorException::class }

}


data class ExceptionDetailsHolder(
        val status: Int,
        val exceptionClass: KClass<*>
)