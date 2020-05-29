package apifi.parser

import apifi.helpers.toCodeGenModel
import apifi.helpers.toTitleCase
import apifi.parser.models.Model
import apifi.parser.models.Property
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import org.openapitools.codegen.CodegenProperty

object ModelParser {
    fun <T> modelsFromSchema(name: String, schema: Schema<T>): List<Model> {
        if (schema is ArraySchema) {
            return modelsFromSchema(name, (schema as ArraySchema).items)
        }
        val codeGenModel = schema.toCodeGenModel(name)
        val models = schema.properties?.filter { shouldCreateModel(it.value) }?.flatMap { modelsFromSchema(toTitleCase(it.key), it.value) }
                ?: emptyList()

        val primaryModel = Model(
                if (schema.type == "object") name else parseReference(schema),
                codeGenModel.allVars?.map {
                    Property(it.name, dataType(it, models), !it.required || it.isNullable)
                } ?: emptyList()
        )
        return listOf(primaryModel) + models
    }

    fun shouldCreateModel(property: Schema<Any>) =
            property is ObjectSchema || (property is ArraySchema && property.items is ObjectSchema)

    private fun dataType(property: CodegenProperty, models: List<Model>) =
            when (property.dataType) {
                "kotlin.Any" -> models.first { m -> m.name == toTitleCase(property.name) }.name
                "kotlin.Array<kotlin.Any>" -> "kotlin.Array<${models.first { m -> m.name == toTitleCase(property.name) }.name}>"
                else -> property.dataType
            }

    fun <T> parseReference(schema: Schema<T>): String {
        val codeGenModel = schema.toCodeGenModel()
        return if (codeGenModel.parent != null) {
            codeGenModel.parent
        } else {
            codeGenModel.dataType
        }
    }
}