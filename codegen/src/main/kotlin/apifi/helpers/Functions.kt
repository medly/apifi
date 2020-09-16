package apifi.helpers

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import org.apache.commons.text.CaseUtils
import org.openapitools.codegen.CodegenModel
import org.openapitools.codegen.languages.KotlinClientCodegen

private fun toCamelCase(s: String, capitalizeFirstLetter: Boolean) = if (listOf('_', '-', '/', ' ').any { s.contains(it) }) CaseUtils.toCamelCase(s, capitalizeFirstLetter, '_', '-', '/', ' ') else s
fun String.toTitleCase(): String = toCamelCase(this.capitalize(), true)
fun String.toCamelCase(): String = toCamelCase(this, false)

fun <T> Schema<T>.toCodeGenModel(name: String): CodegenModel = KotlinClientCodegen().fromModel(name, this)
fun <T> Schema<T>.toCodeGenModel(): CodegenModel = this.toCodeGenModel("any")
fun <T> Schema<T>.typeDeclaration(): String = KotlinClientCodegen().getTypeDeclaration(this)
fun <T> Schema<T>.isEnum(): Boolean = this is StringSchema && enum != null

fun String.toKotlinPoetType(): TypeName = toKotlinPoetType(emptyMap())

fun String.toKotlinPoetType(packageNameMapping: Map<String, String>): TypeName {
    val withPackage = { name: String -> ClassName.bestGuess(packageNameMapping[name] ?: name) }
    return if (this.contains("<")) {
        val parts = this.split('<')
        val primaryType = parts[0]
        val parameters = parts[1].dropLast(1).split(",")
        withPackage(primaryType).parameterizedBy(parameters.map { withPackage(it.trim()) })
    } else {
        withPackage(this)
    }
}