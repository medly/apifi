package apifi.helpers

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.text.CaseUtils
import org.openapitools.codegen.CodegenModel
import org.openapitools.codegen.languages.KotlinClientCodegen

fun toTitleCase(s: String): String = CaseUtils.toCamelCase(s, true, '_', '-', '/')

fun toCamelCase(s: String): String = CaseUtils.toCamelCase(s, false, '_', '-', ' ', '/')

fun <T> Schema<T>.toCodeGenModel(name: String): CodegenModel = KotlinClientCodegen().fromModel(name, this, null)
fun <T> Schema<T>.toCodeGenModel(): CodegenModel = this.toCodeGenModel("any")

fun String.toKotlinPoetType(): TypeName = toKotlinPoetType(emptyMap())

fun String.toKotlinPoetType(packageNameMapping: Map<String, String>): TypeName {
    val withPackage = { name: String -> ClassName.bestGuess(packageNameMapping[name] ?: name) }
    return if (this.contains("<")) {
        val parts = this.split('<')
        val primaryType = parts[0]
        val parameters = parts[1].dropLast(1).split(",")
        withPackage(primaryType).parameterizedBy(parameters.map { withPackage(it) })
    } else {
        withPackage(this)
    }
}
