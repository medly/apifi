package apifi.micronaut.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.lang.Exception
import javax.inject.Singleton

open class ForbiddenException(
        message: String
) : Exception(message)

@Singleton
@Produces
@Requires(classes = [ForbiddenException::class, ExceptionHandler::class])
class DefaultForbiddenExceptionHandler : ExceptionHandler<ForbiddenException, HttpResponse<String>> {
    override fun handle(request: HttpRequest<Any>?, exception: ForbiddenException?):
            HttpResponse<String> {
        val msg = exception?.cause?.localizedMessage ?: "Forbidden Request"
        return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(403), msg)
    }
}
