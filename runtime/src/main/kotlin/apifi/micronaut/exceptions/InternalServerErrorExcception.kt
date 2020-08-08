package apifi.micronaut.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.lang.Exception
import javax.inject.Singleton

open class InternalServerErrorException(
        message: String
) : Exception(message)

@Singleton
@Produces
@Requires(classes = [InternalServerErrorException::class, ExceptionHandler::class])
class DefaultInternalServerErrorExceptionHandler : ExceptionHandler<InternalServerErrorException,
        HttpResponse<String>> {
    override fun handle(request: HttpRequest<Any>?, exception: InternalServerErrorException?):
            HttpResponse<String> {
        val msg = exception?.cause?.localizedMessage ?: "Internal server error occured"
        return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(500), msg)
    }
}
