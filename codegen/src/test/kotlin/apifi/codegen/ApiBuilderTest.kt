package apifi.codegen

import apifi.parser.models.*
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.PathItem

class ApiBuilderTest : DescribeSpec({

    val securityProvider = mockk<SecurityProvider>()
    beforeTest {
        every { securityProvider.shouldAuthenticate(any()) } returns false
    }

    describe("Api Builder") {
        it("generate api class with controller annotation") {
            val path = Path("/pets", listOf(Operation(PathItem.HttpMethod.GET, "listPets", emptyTags(), emptyParams(), null, emptyResponses())))
            val api = ApiBuilder().build("pets", listOf(path), "apifi.gen", testModelMapping(), securityProvider)
            val apiClass = api.members[0] as TypeSpec
            apiClass.name shouldBe "PetsApi"
            apiClass.annotationSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Controller"
        }

        it("generate api methods based on spec") {
            val path1 = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
            ))
            val path2 = Path("/pets/{petId}", listOf(
                    Operation(PathItem.HttpMethod.GET, "getPet", emptyTags(), emptyParams(), null, emptyResponses(), SecurityDefinitionType.BASIC_AUTH)
            ))
            val api = ApiBuilder().build("pets", listOf(path1, path2), "apifi.gen", testModelMapping(), securityProvider)
            val apiClass = api.members[0] as TypeSpec
            apiClass.funSpecs.size shouldBe 3
            apiClass.funSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets\")\n" +
                    "fun getOpName() = HttpResponse.ok(controller.getOpName())\n"
            apiClass.funSpecs[1].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
                    "fun postOpName() = HttpResponse.ok(controller.postOpName())\n"
            apiClass.funSpecs[2].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets/{petId}\")\n" +
                    "fun getPet() = HttpResponse.ok(controller.getPet())\n"
        }

        it("inject controller") {
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", emptyTags(), emptyParams(), Request("Pet", emptyList()), listOf(Response("200", "PetResponse")))

            val api = ApiBuilder().build("pets", listOf(Path("/pets", listOf(operation))), "apifi.gen", testModelMapping(), securityProvider)

            val apiClass = api.members[0] as TypeSpec
            val controllerClass = api.members[1] as TypeSpec
            apiClass.name shouldBe "PetsApi"
            controllerClass.name shouldBe "PetsController"

            apiClass.propertySpecs[0].name shouldBe "controller"
            apiClass.propertySpecs[0].type.toString() shouldBe "apifi.gen.PetsController"
            apiClass.propertySpecs[0].modifiers shouldContain KModifier.PRIVATE

            apiClass.toString() shouldContain "@io.micronaut.http.annotation.Controller\n" +
                    "class PetsApi @javax.inject.Inject constructor(\n" +
                    "  private val controller: apifi.gen.PetsController\n" +
                    ")"
        }
    }

})