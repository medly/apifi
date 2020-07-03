package apifi.parser.models

import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.security.SecurityScheme
import org.apache.http.HttpStatus

data class Spec(val paths: List<Path>, val models: List<Model>, val securityRequirements: List<String>, val securityDefinitions: List<SecurityDefinition>)

data class Path(val url: String, val operations: List<Operation>?)

data class Operation(
        val type: PathItem.HttpMethod,
        val name: String,
        val tags: List<String>?,
        val params: List<Param>?,
        val request: Request?,
        val response: List<Response>?,
        val securitySchemeType: SecurityDefinitionType = SecurityDefinitionType.BASIC_AUTH
)

data class Response(
        val defaultOrStatus: String,
        val type: String
)


data class Request(
        val type: String,
        val consumes: List<String>?
)

data class Param(
        val name: String,
        val dataType: String,
        val isRequired: Boolean,
        val type: ParamType
)

enum class ParamType {
    Query, Path, Header;

    companion object {
        fun fromString(typeInString: String): ParamType =
                when (typeInString) {
                    "path" -> Path
                    "query" -> Query
                    "header" -> Header
                    else -> error("Invalid param type")

                }
    }
}

data class SecurityDefinition(val name: String, val type: SecurityDefinitionType)

enum class SecurityDefinitionType {
    BASIC_AUTH, BEARER;

    companion object {
        fun fromTypeAndScheme(type: SecurityScheme.Type, scheme: String) =
                when {
                    type == SecurityScheme.Type.HTTP && scheme == "basic" -> BASIC_AUTH
                    type == SecurityScheme.Type.HTTP && scheme == "bearer" -> BEARER
                    else -> error("Security scheme not supported yet")
                }
    }
}