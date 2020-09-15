package apifi.codegen

import apifi.helpers.toTitleCase
import apifi.parser.models.Operation
import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec

class ApiMethodBuilder {

    fun methodFor(url: String, operation: Operation, modelMapping: Map<String, String>, securityDefinitions: List<SecurityDefinition>): FunSpec {

        val httpMethodAnnotation = AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, operation.type.toString().toLowerCase().toTitleCase()))
            .addMember("value = %S", url)
            .build()

        val contentTypeAnnotation = operation.request?.consumes?.let {
            AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "Consumes"))
                .also { ab -> it.forEach { ab.addMember("%S", it) } }
                .build()
        }

        val securityParameters =
            when {
                securityDefinitions.any { it.type == SecurityDefinitionType.OIDC } ->
                    listOf(ParameterSpec.builder("authentication", ClassName(micronautSecurityAuthenticationPackage, "Authentication").copy(nullable = true)).build())
                else -> emptyList()
            }

        val returnStatement = "HttpResponse.ok(controller.${operation.name}(${(
            operation.queryParamSpecNames(modelMapping)
                + operation.pathParamSpecNames(modelMapping)
                + operation.requestParamNames(modelMapping)
                + securityParameters.map { it.name }).joinToString()}))"

        return FunSpec.builder(operation.name)
            .also { b -> operation.responses.let { b.addAnnotations(ExceptionAnnotationBuilder().exceptionAnnotationsFor(it)) } }
            .addAnnotation(httpMethodAnnotation)
            .also { b -> contentTypeAnnotation?.let { b.addAnnotation(it) } }
            .addParameters(operation.queryParamSpecs(modelMapping) + operation.pathParamSpecs(modelMapping) + operation.headerParamSpecs() + operation.requestParams(modelMapping) + securityParameters)
            .also { operation.returnType(modelMapping)?.let { rt -> it.returns(rt) } }
            .addStatement("return $returnStatement")
            .build()
    }

}

