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

class ControllerBuilderTest : DescribeSpec( {

    describe("Controller Builder") {
        it("generate controller class with controller annotation") {
            val path = Path("/pets", listOf(Operation(PathItem.HttpMethod.GET, "listPets", null, null, null)))
            val controller = ControllerBuilder.build(path, emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.name shouldBe "PetsController"
            controllerClass.annotationSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Controller"
        }

        it("generate controller method based on spec operation method and url") {
            val path = Path("/pets", listOf(Operation(PathItem.HttpMethod.POST, "listPets", null, null, null)))
            val controller = ControllerBuilder.build(path, emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
            controllerClass.funSpecs[0].name shouldBe "listPets"
        }
        it("generate controller method with query, path and header params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", listOf(queryParam, pathParam, headerParam), null, null)

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                            "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                            "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
        }

        it("generate controller method with request and response") {
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", listOf(), Request("Pet", listOf("application/json", "text/plain")), listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

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
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", listOf(queryParam, pathParam, headerParam), Request("Pet", listOf("application/json")), listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), listOf(SecurityDependency("httpBasic", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].body.toString().trimIndent() shouldBe  "return basicauthorizer.authorize(httpRequest.headers.authorization){HttpResponse.ok(service.listPets(limit, petId, body))}"
        }

        it("generate controller method block with all blocks when security dependencies are not present") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, "createPet", listOf(queryParam, pathParam, headerParam), Request("Pet", null), listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].body.toString().trimIndent() shouldBe  "return HttpResponse.ok(service.createPet(limit, petId, body))"
        }

        it("inject service & security dependencies") {
            val operation = Operation(PathItem.HttpMethod.POST, "listPets", listOf(), Request("Pet", null), listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), listOf(SecurityDependency("BasicAuthorizer", "security", SecurityDefinitionType.BASIC_AUTH)), "apifi.gen", modelMapping())

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

fun modelMapping() = listOf("Pet" to "models.Pet", "PetResponse" to "models.PetResponse")
