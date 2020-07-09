package apifi.codegen

import apifi.codegen.exceptions.ExceptionDetailsHolder
import apifi.codegen.exceptions.ExceptionFileBuilder
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.matchers.collections.shouldNotContainInOrder
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.DescribeSpec

class ExceptionFileBuilderTest : DescribeSpec( {

    describe("should generate exception and global exception handler for a given exception") {

        val exceptionFileSpec = ExceptionFileBuilder.build(ExceptionDetailsHolder(123, "SomeException", "Some error occurred"), "com.abc")

        it("should generate exception class") {
            exceptionFileSpec.members[0].toString() shouldBe
                    "class SomeException(\n" +
                    "  message: kotlin.String\n" +
                    ") : java.lang.Exception(message)\n"
        }

        it("should generate global exception handler for the exception class with exception's default message and status") {
            exceptionFileSpec.members[1].toString() shouldContain
                    "class GlobalSomeExceptionHandler : io.micronaut.http.server.exceptions.ExceptionHandler<com.abc.exceptions.SomeException, io.micronaut.http.HttpResponse<String>> {\n" +
                    "  fun handle(request: io.micronaut.http.HttpRequest<Any>?, exception: com.abc.exceptions.SomeException?): io.micronaut.http.HttpResponse<String> {\n" +
                    "    val msg = exception?.conversionError?.cause?.localizedMessage ?: \"Some error occurred\"\n" +
                    "    HttpResponse.status<String>(HttpStatus.valueOf(123), msg)\n" +
                    "  }\n" +
                    "}"
        }

        it("exception handler class should have all required annotations") {
            val exceptionHandlerClass = exceptionFileSpec.members[1] as TypeSpec
            val annotations = exceptionHandlerClass.annotationSpecs.map { it.toString() }

            annotations shouldNotContainInOrder listOf( "@javax.inject.Singleton",
                                                        "@io.micronaut.http.annotation.Produces",
                    "@io.micronaut.context.annotation.Requires(classes = [com.abc.SomeException::class, io.micronaut.http.server.exceptions.ExceptionHandler::class])")
        }
    }

})