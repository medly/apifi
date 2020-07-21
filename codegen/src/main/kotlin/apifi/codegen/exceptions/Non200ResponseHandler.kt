package apifi.codegen.exceptions

import org.apache.http.HttpStatus

object Non200ResponseHandler {

    private val allExceptionDetailsHolder = listOf(
            ExceptionDetailsHolder(HttpStatus.SC_BAD_REQUEST, "BadRequestException", "Bad Request"),
            ExceptionDetailsHolder(HttpStatus.SC_UNAUTHORIZED, "UnauthorizedException", "Unauthorized Request"),
            ExceptionDetailsHolder(HttpStatus.SC_FORBIDDEN, "ForbiddenException", "Forbidden Request"),
            ExceptionDetailsHolder(HttpStatus.SC_NOT_FOUND, "NotFoundException", "Request Not Found"),
            ExceptionDetailsHolder(HttpStatus.SC_INTERNAL_SERVER_ERROR, "InternalServerErrorException", "Internal server error occured")
    )

    fun getExceptionClassFor(statuses: List<Int>) = statuses.map { status -> allExceptionDetailsHolder.find { it.status == status }?.exceptionClassName ?: "InternalServerErrorException" }

    fun generateExceptionClassesAndHandlers(basePackageName: String) =
        allExceptionDetailsHolder.map { exception ->
            ExceptionFileBuilder.build(exception, basePackageName)
        }


}


data class ExceptionDetailsHolder(
        val status: Int,
        val exceptionClassName: String,
        val defaultExceptionMessage: String
)