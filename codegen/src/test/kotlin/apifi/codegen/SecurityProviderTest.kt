package apifi.codegen

import apifi.parser.models.Operation
import apifi.parser.models.SecurityDefinition
import apifi.parser.models.SecurityDefinitionType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.PathItem

class SecurityProviderTest: StringSpec() {
    init {
        "should require authentication for OIDC scheme" {
            val securityProvider = SecurityProvider(listOf(SecurityDefinition("openid", SecurityDefinitionType.OIDC)), listOf("openid"))
            securityProvider.shouldAuthenticate(Operation(PathItem.HttpMethod.GET, "get", request = null)) shouldBe true
        }

        "should not require authentication for OIDC scheme if not specified in security requirements" {
            val securityProvider = SecurityProvider(listOf(SecurityDefinition("openid", SecurityDefinitionType.OIDC)), listOf("openid2"))
            securityProvider.shouldAuthenticate(Operation(PathItem.HttpMethod.GET, "get", request = null)) shouldBe false
        }

        "should not require authentication for other scheme even if specified in security requirements" {
            val securityProvider = SecurityProvider(listOf(SecurityDefinition("basic", SecurityDefinitionType.BASIC_AUTH)), listOf("basic"))
            securityProvider.shouldAuthenticate(Operation(PathItem.HttpMethod.GET, "get", request = null)) shouldBe false
        }
    }
}
