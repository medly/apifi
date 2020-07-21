package apifi.micronaut.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.lang.Exception
import javax.inject.Singleton

open class NotFoundException(
        message: String
) : Exception(message)

@Singleton
@Produces
@Requires(classes = [NotFoundException::class, ExceptionHandler::class])
class DefaultNotFoundExceptionHandler : ExceptionHandler<NotFoundException, HttpResponse<String>> {
    override fun handle(request: HttpRequest<Any>?, exception: NotFoundException?):
            HttpResponse<String> {
        val msg = exception?.cause?.localizedMessage ?: "Request Not Found"
        return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(404), msg)
    }
}
