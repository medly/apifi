package apifi.parser.models

import apifi.models.Model
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityScheme

data class Spec(val paths: List<Path>, val models: List<Model>, val securityRequirements: List<String>, val securityDefinitions: List<SecurityDefinition>)

data class Path(val url: String, val operations: List<Operation>?)

data class Operation(
        val type: PathItem.HttpMethod,
        val name: String,
        val tags: List<String> = emptyList(),
        val params: List<Param> = emptyList(),
        val request: Request?,
        val responses: List<Response> = emptyList(),
        val securitySchemeType: SecurityDefinitionType = SecurityDefinitionType.BASIC_AUTH
) {
    fun hasOnlyDefaultResponse(): Boolean = responses.size == 1 && responses.first().defaultOrStatus == ApiResponses.DEFAULT

    fun hasMoreThanOne2xxResponse(): Boolean = responses.size > 1 && responses.map { it.defaultOrStatus }.all { it.startsWith("2") }

    fun first2xxResponse(): Response? = responses.firstOrNull { it.defaultOrStatus.startsWith("2") }
}

data class Response(
        val defaultOrStatus: String,
        val type: String
)


data class Request(
        val type: String,
        val consumes: List<String> = emptyList()
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
    BASIC_AUTH, BEARER, OIDC;

    companion object {
        fun fromTypeAndScheme(type: SecurityScheme.Type, scheme: String?) =
                when {
                    type == SecurityScheme.Type.HTTP && scheme == "basic" -> BASIC_AUTH
                    type == SecurityScheme.Type.HTTP && scheme == "bearer" -> BEARER
                    type == SecurityScheme.Type.OPENIDCONNECT -> OIDC
                    else -> error("Security scheme not supported yet")
                }
    }
}