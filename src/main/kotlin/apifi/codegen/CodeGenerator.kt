package apifi.codegen

import apifi.parser.models.SecurityDefinitionType
import apifi.parser.models.Spec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

object CodeGenerator {
    fun generate(spec: Spec, basePackageName: String): List<FileContent> {
        val modelFiles: List<FileSpec> = spec.models?.let { listOf(ModelFileBuilder.build(it, basePackageName)) } ?: emptyList()
        val modelMapping = modelFiles.flatMap { it.members.mapNotNull { m -> (m as TypeSpec).name }.map { name -> name to "${it.packageName}.$name" } }

        val securityFiles = spec.securityDefinitions?.find { it.type == SecurityDefinitionType.BASIC_AUTH }?.let {
            mapOf(SecurityDefinitionType.BASIC_AUTH to BasicAuthSecurityStubBuilder.build(basePackageName))
        } ?: emptyMap()

        val controllerFiles = spec.paths?.map { path ->
            ControllerBuilder.build(path, securityFiles.map { (type, spec) -> SecurityDependency((spec.members.first() as TypeSpec).name!!, spec.packageName, type) }, basePackageName, modelMapping)
        } ?: emptyList()

        return (controllerFiles + modelFiles + securityFiles.entries.map{it.value})
                .map { FileContent(it.name, it.packageName.replace(".", File.separator), it.toString()) }
    }

}

data class ControllerProperty(val name: String, val packageName: String)

data class SecurityDependency(val name: String, val packageName: String, val securityDefinitionType: SecurityDefinitionType)

data class FileContent(
        val name: String,
        val location: String,
        val content: String
)