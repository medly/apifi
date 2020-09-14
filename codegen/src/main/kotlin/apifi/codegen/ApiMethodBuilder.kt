package apifi.codegen

import apifi.helpers.toTitleCase
import apifi.parser.models.Operation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec


class ApiMethodBuilder {

    fun methodFor(url: String, operation: Operation, modelMapping: Map<String, String>): FunSpec {

        val httpMethodAnnotation = AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, operation.type.toString().toLowerCase().toTitleCase()))
                                                               .addMember("value = %S", url)
                                                               .build()

        val contentTypeAnnotation = operation.request?.consumes?.let {  AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "Consumes"))
                                                                .also { ab -> it.forEach { ab.addMember("%S", it) } }
                                                                .build()}

        val returnStatement =  "HttpResponse.ok(controller.${operation.name}(${(operation.queryParamSpecNames() + operation.pathParamSpecNames() + operation.requestParamNames(modelMapping)).joinToString()}))"



        return FunSpec.builder(operation.name)
                .also { b -> operation.responses.let { b.addAnnotations(ExceptionAnnotationBuilder().exceptionAnnotationsFor(it)) } }
                .addAnnotation(httpMethodAnnotation)
                .also { b -> contentTypeAnnotation?.let { b.addAnnotation(it) } }
                .addParameters(operation.queryParamSpecs() + operation.pathParamSpecs() + operation.headerParamSpecs() + operation.requestParams(modelMapping))
                .also { operation.returnType(modelMapping)?.let { rt -> it.returns(rt) } }
                .addStatement("return $returnStatement")
                .build()
    }

}

