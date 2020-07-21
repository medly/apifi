package apifi.micronaut.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.lang.Exception
import javax.inject.Singleton

open class UnauthorizedException(
        message: String
) : Exception(message)

@Singleton
@Produces
@Requires(classes = [UnauthorizedException::class, ExceptionHandler::class])
class DefaultUnauthorizedExceptionHandler : ExceptionHandler<UnauthorizedException,
        HttpResponse<String>> {
    override fun handle(request: HttpRequest<Any>?, exception: UnauthorizedException?):
            HttpResponse<String> {
        val msg = exception?.cause?.localizedMessage ?: "Unauthorized Request"
        return HttpResponse.status<String>(io.micronaut.http.HttpStatus.valueOf(401), msg)
    }
}
