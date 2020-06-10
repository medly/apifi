package apifi.codegen

import apifi.codegen.security.BasicAuthSecurityStubBuilder
import apifi.codegen.security.BearerAuthSecurityStubBuilder
import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import apifi.parser.models.Spec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

object CodeGenerator {
    fun generate(spec: Spec, basePackageName: String): List<FileSpec> {
        val modelFiles: List<FileSpec> = if(spec.models.isNotEmpty()) listOf(ModelFileBuilder.build(spec.models, basePackageName)) else emptyList()
        val responseModelFile = ResponseModelBuilder.build(basePackageName)
        val modelMapping = (modelFiles + responseModelFile).flatMap { it.members.mapNotNull { m -> (m as TypeSpec).name }.map { name -> name to "${it.packageName}.$name" } }
        val securityFiles = spec.securityDefinitions.fold<SecurityDefinition, Map<SecurityDefinition, FileSpec>>(mapOf(), { acc, securityDefinition ->
            when(securityDefinition.type) {
                SecurityDefinitionType.BASIC_AUTH -> acc + mapOf(securityDefinition to BasicAuthSecurityStubBuilder.build(basePackageName))
                SecurityDefinitionType.BEARER -> acc + mapOf(securityDefinition to BearerAuthSecurityStubBuilder.build(basePackageName))
            }
        })

        val securityDependencies = securityFiles
                .filter { spec.securityRequirements.contains(it.key.name) }
                .map { (def, spec) -> SecurityDependency((spec.members.first() as TypeSpec).name!!, spec.packageName, def.type) }

        val apiGroups = spec.paths.groupBy { it.operations?.firstOrNull { o -> o.tags != null }?.tags?.firstOrNull() }.filter { it.key != null }

        val apiClassFiles = apiGroups.map { ApiBuilder.build(it.key!!, it.value, securityDependencies, basePackageName, modelMapping) }

        return (apiClassFiles + modelFiles + responseModelFile)
    }

}

data class SecurityDependency(val name: String, val packageName: String, val securityDefinitionType: SecurityDefinitionType)
