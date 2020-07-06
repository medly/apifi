package apifi.codegen

import apifi.helpers.toTitleCase
import apifi.models.Operation
import apifi.models.Path
import apifi.models.Response
import com.squareup.kotlinpoet.*

object ApiBuilder {

    private const val micronautHttpAnnotation = "io.micronaut.http.annotation"

    fun build(name: String, paths: List<Path>, basePackageName: String, modelMapping: Map<String, String>): FileSpec {
        val baseName = toTitleCase(name)
        val controllerClassName = "${baseName}Api"

        val controllerInterfaceClass = ControllerInterfaceBuilder.build(paths, baseName)

        val primaryConstructor = FunSpec.constructorBuilder()
                .addAnnotation(ClassName("javax.inject", "Inject"))
                .addParameter(ParameterSpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!)).build())

        val controllerProperty = PropertySpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!))
                .addModifiers(KModifier.PRIVATE).initializer("controller").build()


        val classSpec = TypeSpec.classBuilder(ClassName(basePackageName, controllerClassName))
                .addAnnotation(AnnotationSpec.builder(ClassName(micronautHttpAnnotation, "Controller"))
                        .build())
                .addProperty(controllerProperty)
                .addFunctions(generateOperationFunctions(paths, basePackageName, modelMapping))

        classSpec.primaryConstructor(primaryConstructor.build())

        return FileSpec.builder(basePackageName, "$controllerClassName.kt").addType(classSpec.build()).addType(controllerInterfaceClass).build()
    }

    private fun generateOperationFunctions(paths: List<Path>, basePackageName: String, modelMapping: Map<String, String>): List<FunSpec> {
        return paths.flatMap { path ->
            path.operations?.map { operation ->
                val serviceCallStatement = controllerCallStatement(operation, modelMapping)
                FunSpec.builder(operation.name)
                        .also { b -> operation.response?.let { b.addAnnotations(operationExceptionAnnotations(it, basePackageName)) } }
                        .addAnnotation(operationTypeAnnotation(operation, path))
                        .also { b -> operation.request?.consumes?.let { consumes -> b.addAnnotation(operationContentTypeAnnotation(consumes)) } }
                        .addParameters(operation.queryParams() + operation.pathParams() + operation.headerParams() + operation.requestParams(modelMapping))
                        .also { operation.returnType(modelMapping)?.let { rt -> it.returns(rt) } }
                        .addStatement("return $serviceCallStatement")
                        .build()
            } ?: emptyList()
        }
    }

    private fun operationTypeAnnotation(operation: Operation, path: Path) =
            AnnotationSpec.builder(ClassName(micronautHttpAnnotation, toTitleCase(operation.type.toString())))
                    .addMember("value = %S", path.url)
                    .build()

    private fun operationContentTypeAnnotation(consumes: List<String>) =
            AnnotationSpec.builder(ClassName(micronautHttpAnnotation, "Consumes"))
                    .also { ab -> consumes.forEach { ab.addMember("%S", it) } }
                    .build()

    private fun operationExceptionAnnotations(responses: List<Response>, basePackageName: String): List<AnnotationSpec> {
        val non2xxStatusResponseFromOperation = responses.filter { it.defaultOrStatus != "default" && it.defaultOrStatus != "200" && it.defaultOrStatus != "201" }.map { it.defaultOrStatus.toInt() }
        val exceptionClassesForNon2xxResponses = non2xxStatusResponseFromOperation.let { Non200ResponseHandler.getExceptionClassFor(it) }
        return exceptionClassesForNon2xxResponses.map { exceptionClass ->
            AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", ClassName("$basePackageName.exceptions", exceptionClass))
                    .build()
        }
    }

    private fun controllerCallStatement(operation: Operation, modelMapping: Map<String, String>): String {
        val queryParamNames = operation.queryParams().map { it.name }
        val pathParamNames = operation.pathParams().map { it.name }
        val requestBodyParams = operation.requestParams(modelMapping)
        val requestParamNames = requestBodyParams.map { it.name }
        return "HttpResponse.ok(controller.${operation.name}(${(queryParamNames + pathParamNames + requestParamNames).joinToString()}))"
    }

}
