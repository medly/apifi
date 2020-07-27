package apifi.codegen

import apifi.helpers.HttpStatusToExceptionClassMapper
import apifi.parser.models.Response
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

class ExceptionAnnotationBuilder(private val httpStatusToExceptionClassMapper: HttpStatusToExceptionClassMapper = HttpStatusToExceptionClassMapper()) {

    fun exceptionAnnotationsFor(responses: List<Response>): List<AnnotationSpec> {
        val non2xxStatusResponseFromOperation = responses.filter { it.defaultOrStatus != "default" && it.defaultOrStatus != "200" && it.defaultOrStatus != "201" }.map { it.defaultOrStatus.toInt() }
        val exceptionClassesForNon2xxResponses = non2xxStatusResponseFromOperation.let { httpStatusToExceptionClassMapper.getExceptionClassFor(it) }
        return exceptionClassesForNon2xxResponses.map { exceptionClass ->
            AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", ClassName(apifiExceptionsPackage, exceptionClass))
                    .build()
        }
    }
}