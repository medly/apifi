package apifi.codegen

import apifi.parser.models.Operation
import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType

class SecurityProvider(
    private val securityDefinitions: List<SecurityDefinition>,
    private val securityRequirements: List<String>
) {
    // TODO: Should use operation's security requirements as well
    fun shouldAuthenticate(operation: Operation): Boolean =
        securityDefinitions.firstOrNull { it.type == SecurityDefinitionType.OIDC }?.let { securityRequirements.contains(it.name) }
            ?: false
}
