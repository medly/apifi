package apifi.codegen

import apifi.helpers.toTitleCase
import apifi.parser.models.Path
import com.squareup.kotlinpoet.*

class ApiBuilder(private val apiMethodBuilder: ApiMethodBuilder = ApiMethodBuilder()) {

    fun build(name: String, paths: List<Path>, basePackageName: String, modelMapping: Map<String, String>): FileSpec {
        val baseName = name.toTitleCase()
        val controllerClassName = "${baseName}Api"

        val controllerInterfaceClass = ControllerInterfaceBuilder.build(paths, baseName)

        val allControllerFunSpecs = paths.flatMap { path -> path.operations?.map { op -> apiMethodBuilder.methodFor(path.url, op, modelMapping) }?: emptyList() }

        val classSpec = TypeSpec.classBuilder(ClassName(basePackageName, controllerClassName))
                .addAnnotation(AnnotationSpec.builder(ClassName(micronautHttpAnnotationPackage, "Controller"))
                        .build())
                .addProperty(PropertySpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!))
                        .addModifiers(KModifier.PRIVATE).initializer("controller").build())
                .addFunctions(allControllerFunSpecs)

        classSpec.primaryConstructor(FunSpec.constructorBuilder()
                .addAnnotation(ClassName(javaxInjectPackage, "Inject"))
                .addParameter(ParameterSpec.builder("controller", ClassName(basePackageName, controllerInterfaceClass.name!!)).build()).build())

        return FileSpec.builder(basePackageName, "$controllerClassName.kt").addType(classSpec.build()).addType(controllerInterfaceClass).build()
    }
}