package apifi.micronaut.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.lang.Exception
import javax.inject.Singleton
import kotlin.Any
import kotlin.String

open class BadRequestException(
        message: String
) : Exception(message)

@Singleton
@Produces
@Requires(classes = [BadRequestException::class, ExceptionHandler::class])
class DefaultBadRequestExceptionHandler : ExceptionHandler<BadRequestException, HttpResponse<String>>
{
    override fun handle(request: HttpRequest<Any>?, exception: BadRequestException?):
            HttpResponse<String> {
        val msg = exception?.cause?.localizedMessage ?: "Bad Request"
        return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(400), msg)
    }
}

