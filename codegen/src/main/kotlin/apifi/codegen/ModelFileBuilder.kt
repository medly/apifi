package apifi.codegen

import apifi.helpers.toKotlinPoetType
import apifi.models.Model
import com.squareup.kotlinpoet.*

object ModelFileBuilder {
    fun build(models: List<Model>, basePackageName: String): FileSpec {
        val packageName = "$basePackageName.models"
        val baseModelMapping = models.map { it.name to "$packageName.${it.name}" }.toMap()
        val builder = FileSpec.builder(packageName, "Models.kt")
        models.forEach { model ->
            builder.addType(
                    TypeSpec.classBuilder(ClassName(packageName, model.name))
                            .addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                    FunSpec.constructorBuilder()
                                            .addParameters(model.properties.map {
                                                ParameterSpec.builder(
                                                        it.name,
                                                        it.dataType.toKotlinPoetType(baseModelMapping).copy(nullable = it.nullable)
                                                ).build()
                                            }).build()
                            )
                            .addProperties(model.properties.map {
                                PropertySpec.builder(it.name, it.dataType.toKotlinPoetType(baseModelMapping).copy(nullable = it.nullable))
                                        .initializer(it.name)
                                        .build()
                            })
                            .build())
        }
        return builder.build()
    }
}