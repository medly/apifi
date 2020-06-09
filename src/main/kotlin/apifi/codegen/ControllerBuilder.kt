package apifi.codegen

import apifi.helpers.toCamelCase
import apifi.helpers.toTitleCase
import apifi.parser.models.Operation
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object ControllerBuilder {

	fun build(name: String, paths: List<Path>, securityDependencies: List<SecurityDependency>, basePackageName: String, modelMapping: Map<String, String>): FileSpec {
		val baseName = toTitleCase(name)
		val controllerClassName = "${baseName}Controller"

		val serviceClass = ServiceBuilder.build(paths, baseName, modelMapping)

		val primaryConstructor = FunSpec.constructorBuilder()
				.addAnnotation(ClassName("javax.inject", "Inject"))
				.addParameter(ParameterSpec.builder("service", ClassName(basePackageName, serviceClass.name!!)).build())

		val serviceProperty = PropertySpec.builder("service", ClassName(basePackageName, serviceClass.name!!))
				.addModifiers(KModifier.PRIVATE).initializer("service").build()

		val classSpec = TypeSpec.classBuilder(ClassName(basePackageName, controllerClassName))
				.addAnnotation(AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", "Controller"))
						.build())
				.addProperty(serviceProperty)
				.addFunctions(generateOperationFunctions(paths, modelMapping, securityDependencies))

		securityDependencies.forEach { dependency ->
			primaryConstructor.addParameter(
					ParameterSpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name)).build()
			)

			classSpec.addProperty(PropertySpec.builder(toCamelCase(dependency.name), ClassName(dependency.packageName, dependency.name))
					.addModifiers(KModifier.PRIVATE).initializer(toCamelCase(dependency.name)).build())
		}

		classSpec.primaryConstructor(primaryConstructor.build())

		return FileSpec.builder(basePackageName, "$controllerClassName.kt").addType(classSpec.build()).addType(serviceClass).build()
	}

	private fun generateOperationFunctions(paths: List<Path>, modelMapping: Map<String, String>, securityDependencies: List<SecurityDependency>): List<FunSpec> {
		return paths.flatMap { path ->
			path.operations?.map { operation ->
				val httpRequestParam = ParameterSpec.builder("httpRequest",
						ClassName("io.micronaut.http", "HttpRequest").parameterizedBy(Any::class.asClassName()))
						.build()
				val serviceCallStatement = OperationStatementBuilder.build(operation, modelMapping)
				FunSpec.builder(operation.name)
						.addAnnotation(operationTypeAnnotation(operation, path))
						.also { b -> operation.request?.consumes?.let { consumes -> b.addAnnotation(operationContentTypeAnnotation(consumes)) } }
						.addParameters(operation.queryParams() + operation.pathParams() + operation.headerParams() + operation.requestParams(modelMapping))
						.addParameter(httpRequestParam)
						.also { it.returns(operation.returnType(modelMapping)) }
						.addStatement(if (securityDependencies.isNotEmpty()) "return basicauthorizer.authorize(httpRequest.headers.authorization){$serviceCallStatement}" else serviceCallStatement)
						.build()
			} ?: emptyList()
		}
	}

	private fun operationTypeAnnotation(operation: Operation, path: Path) =
			AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", toTitleCase(operation.type.toString())))
					.addMember("value = %S", path.url)
					.build()

	private fun operationContentTypeAnnotation(consumes: List<String>) =
			AnnotationSpec.builder(ClassName("io.micronaut.http.annotation", "Consumes"))
					.also { ab -> consumes.forEach { ab.addMember("%S", it) } }
					.build()

}
