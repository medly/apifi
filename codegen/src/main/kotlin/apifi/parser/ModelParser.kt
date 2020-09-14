package apifi.parser

import apifi.helpers.isEnum
import apifi.helpers.toCodeGenModel
import apifi.helpers.toTitleCase
import apifi.models.Model
import apifi.models.Property
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import org.openapitools.codegen.CodegenProperty

object ModelParser {
    fun <T> modelsFromSchema(name: String, schema: Schema<T>): List<Model> {
        if (schema is ArraySchema) {
            return modelsFromSchema(name, (schema as ArraySchema).items)
        }
        val codeGenModel = schema.toCodeGenModel(name)
        val propertyModels = schema.properties?.filter { shouldCreateModel(it.value) }?.flatMap { modelsFromSchema(toTitleCase(it.key), it.value) }
            ?: emptyList()

        val primaryModel = Model(
            if (schema.type == "object" || schema.isEnum()) name else parseReference(schema),
            codeGenModel.allVars?.map {
                Property(it.name, dataType(it, propertyModels), !it.required || it.isNullable)
            } ?: emptyList(),
            (schema as? StringSchema)?.enum ?: emptyList()
        )
        return listOf(primaryModel) + propertyModels
    }

    fun shouldCreateModel(property: Schema<Any>) =
        property is ObjectSchema || (property is ArraySchema && property.items is ObjectSchema) || property.isEnum()

    private fun dataType(property: CodegenProperty, models: List<Model>) =
        models.firstOrNull { m -> m.name == toTitleCase(property.name) }?.name?.let {
            if (property.isListContainer) "kotlin.Array<$it>" else it
        } ?: property.dataType

    fun <T> parseReference(schema: Schema<T>): String {
        val codeGenModel = schema.toCodeGenModel()
        return if (codeGenModel.parent != null) {
            codeGenModel.parent
        } else {
            codeGenModel.dataType
        }
    }
}