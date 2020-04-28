package apifi.parser

import apifi.parser.models.Model
import apifi.parser.models.Property
import apifi.helpers.toCodeGenModel
import io.swagger.v3.oas.models.media.Schema

object ModelParser {
        fun <T> modelFromSchema(name: String, schema: Schema<T>): Model {
            val codeGenModel = schema.toCodeGenModel(name)
            return Model(
                    if (schema.type == "object") name else codeGenModel.dataType,
                    codeGenModel.allVars.map { Property(it.name, it.dataType, !it.required || it.isNullable) }
            )
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