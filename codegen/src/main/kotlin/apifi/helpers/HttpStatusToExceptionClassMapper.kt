package apifi.helpers


class HttpStatusToExceptionClassMapper {

    private val allExceptionDetailsHolder = listOf(
            ExceptionDetailsHolder(400, "BadRequestException"),
            ExceptionDetailsHolder(401, "UnauthorizedException"),
            ExceptionDetailsHolder(403, "ForbiddenException"),
            ExceptionDetailsHolder(404, "NotFoundException"),
            ExceptionDetailsHolder(500, "InternalServerErrorException")
    )

    class ExceptionDetailsHolder(
            val status: Int,
            val exceptionClassPath: String
    )

    fun getExceptionClassFor(statuses: List<Int>) = statuses.map { status -> allExceptionDetailsHolder.find { it.status == status }?.exceptionClassPath ?: "InternalServerErrorException" }

}


