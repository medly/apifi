package apifi.parser

import apifi.helpers.toCodeGenModel
import apifi.helpers.toTitleCase
import apifi.parser.models.Model
import apifi.parser.models.Property
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema

object ModelParser {
    fun <T> modelFromSchema(name: String, schema: Schema<T>): Model {
        val codeGenModel = schema.toCodeGenModel(name)
        return Model(
                if (schema.type == "object") name else codeGenModel.dataType,
                codeGenModel.allVars.map { Property(it.name, it.dataType, !it.required || it.isNullable) }
        )
    }

    fun <T> modelsFromSchema(name: String, schema: Schema<T>): List<Model> {
        val codeGenModel = schema.toCodeGenModel(name)
        val models = schema.properties.filter { it.value is ObjectSchema }.flatMap { modelsFromSchema(toTitleCase(it.key), it.value) }

        val primaryModel = Model(
                if (schema.type == "object") name else codeGenModel.dataType,
                codeGenModel.allVars.map {
                    Property(it.name, if(it.dataType == "kotlin.Any") models.first { m -> m.name == toTitleCase(it.name) }.name else it.dataType, !it.required || it.isNullable)
                }
        )
        return listOf(primaryModel) + models
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