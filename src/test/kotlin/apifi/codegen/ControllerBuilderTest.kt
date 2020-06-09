package apifi.codegen

import apifi.parser.models.*
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ControllerBuilderTest : DescribeSpec({

    describe("Controller Builder") {
        it("generate controller class with controller annotation") {
            val path = Path("/pets", listOf(Operation(PathItem.HttpMethod.GET, "listPets", emptyList(), null, null, null)))
            val controller = ControllerBuilder.build("pets", listOf(path), emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.name shouldBe "PetsController"
            controllerClass.annotationSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Controller"
        }

        it("generate controller method based on spec operation method and url") {
            val path1 = Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val path2 = Path("/pets/{petId}", listOf(
                    Operation(PathItem.HttpMethod.GET, "getPet", emptyList(), null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))
            val controller = ControllerBuilder.build("pets", listOf(path1, path2), emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs.size shouldBe 3
            controllerClass.funSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets\")\n" +
                    "fun getOpName(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) {\n" +
					"  service.getOpName()\n" +
					"}\n"
            controllerClass.funSpecs[1].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")\n" +
					"fun postOpName(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) {\n" +
					"  service.postOpName()\n" +
					"}\n"
            controllerClass.funSpecs[2].toString() shouldBe "@io.micronaut.http.annotation.Get(value = \"/pets/{petId}\")\n" +
					"fun getPet(httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>) {\n" +
					"  service.getPet()\n" +
					"}\n"
        }
        it("generate controller method with query, path and header params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), null, null)

            val controller = ControllerBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                            "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                            "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
        }

        it("generate controller method with request and response") {
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), emptyList(), Request("Pet", listOf("application/json", "text/plain")), listOf(Response("200", "PetResponse")))

            val controller = ControllerBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
            controllerClass.funSpecs[0].annotations[1].toString() shouldBe "@io.micronaut.http.annotation.Consumes(\"application/json\", \"text/plain\")"
            controllerClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.Body body: models.Pet",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
            controllerClass.funSpecs[0].returnType.toString() shouldBe "io.micronaut.http.HttpResponse<models.PetResponse>"
        }

        it("generate controller method block with all blocks when security dependencies are present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf(Response("200", "PetResponse")))

            val controller = ControllerBuilder.build("pets", listOf(Path("/pets", listOf(operation))), listOf(SecurityDependency("httpBasic", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].body.toString().trimIndent() shouldBe "return basicauthorizer.authorize(httpRequest.headers.authorization){val response = service.listPets(limit, petId, body)\n" +
					"    \t\t\treturn HttpResponse.ok(response.body)}"
        }

        it("generate controller method block with all blocks when security dependencies are not present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", emptyList(), listOf(queryParam, pathParam, headerParam), Request("Pet", null), listOf(Response("200", "PetResponse")))

            val controller = ControllerBuilder.build("pets", listOf(Path("/pets", listOf(operation))), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].body.toString().trimIndent() shouldBe "val response = service.createPet(limit, petId, body)\n" +
					"    \t\t\treturn HttpResponse.ok(response.body)"
        }

        it("inject service & security dependencies") {
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", listOf(), emptyList(), Request("Pet", null), listOf(Response("200", "PetResponse")))

            val controller = ControllerBuilder.build("pets", listOf(Path("/pets", listOf(operation))), listOf(SecurityDependency("BasicAuthorizer", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            val serviceClass = controller.members[1] as TypeSpec
            controllerClass.name shouldBe "PetsController"
            serviceClass.name shouldBe "PetsService"

            controllerClass.propertySpecs[0].name shouldBe "service"
            controllerClass.propertySpecs[0].type.toString() shouldBe "apifi.gen.PetsService"
            controllerClass.propertySpecs[0].modifiers shouldContain KModifier.PRIVATE

            controllerClass.toString() shouldContain "@io.micronaut.http.annotation.Controller\n" +
                    "class PetsController @javax.inject.Inject constructor(\n" +
                    "  private val service: apifi.gen.PetsService,\n" +
                    "  private val basicauthorizer: security.BasicAuthorizer\n" +
                    ")"
        }
    }


})

fun modelMapping() = mapOf("Pet" to "models.Pet", "PetResponse" to "models.PetResponse")
