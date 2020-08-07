package apifi.codegen

import apifi.parser.models.Response
import com.squareup.kotlinpoet.AnnotationSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class ExceptionAnnotationBuilderTest : StringSpec({

    val exceptionAnnotationBuilder = ExceptionAnnotationBuilder()
    "should not return any annotations if only responses for 200, 201 and default are specified" {
        forAll(
                row(listOf(Response("200", "PetResponse")), emptyList()),
                row(listOf(Response("201", "kotlin.String")), emptyList()),
                row(listOf(Response("default", "PetResponse")), emptyList()),
                row(listOf(Response("200", "PetResponse"), Response("201", "kotlin.String"), Response("default", "PetResponse")), emptyList())
        ) { responses, annotations: List<AnnotationSpec> ->
                exceptionAnnotationBuilder.exceptionAnnotationsFor(responses) shouldBe annotations
        }
    }

    "should return annotation respective to http status for error response codes" {
        forAll(
                row(listOf(Response("400", "kotlin.String")), listOf("@kotlin.jvm.Throws(apifi.micronaut.exceptions.BadRequestException::class)")),
                row(listOf(Response("503", "kotlin.String")), listOf("@kotlin.jvm.Throws(apifi.micronaut.exceptions.InternalServerErrorException::class)"))
        ) { responses, annotations: List<String> ->
            exceptionAnnotationBuilder.exceptionAnnotationsFor(responses).map { it.toString() } shouldBe annotations
        }
    }

})