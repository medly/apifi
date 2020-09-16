package apifi.codegen

import apifi.parser.models.Spec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class CodeGenerator {
    fun generate(spec: Spec, basePackageName: String): List<FileSpec> {
        val modelFiles: List<FileSpec> = if (spec.models.isNotEmpty()) listOf(ModelFileBuilder.build(spec.models, basePackageName)) else emptyList()
        val modelMapping = modelFiles.flatMap { it.members.mapNotNull { m -> (m as TypeSpec).name }.map { name -> name to "${it.packageName}.$name" } }.toMap()
        val apiGroups = spec.paths.groupBy { it.operations?.firstOrNull()?.tags?.firstOrNull() }.filter { it.key != null }
        val securityProvider = SecurityProvider(spec.securityDefinitions, spec.securityRequirements)
        val apiBuilder = ApiBuilder(ApiMethodBuilder(modelMapping, securityProvider), basePackageName, securityProvider)
        val apiClassFiles = apiGroups.map { apiBuilder.build(it.key!!, it.value) }

        return (apiClassFiles + modelFiles)
    }

}
