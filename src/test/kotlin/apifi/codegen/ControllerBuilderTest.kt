package apifi.codegen

import apifi.parser.models.Operation
import apifi.parser.models.Param
import apifi.parser.models.ParamType
import apifi.parser.models.Path
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ControllerBuilderTest : DescribeSpec( {

    describe("Controller Builder") {
        it("generate controller class with controller annotation") {
            val controller = ControllerBuilder.build(Path("/pets", listOf(Operation(PathItem.HttpMethod.GET, null, null, null))), emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.name shouldBe "PetsController"
            controllerClass.annotationSpecs[0].toString() shouldBe "@io.micronaut.http.annotation.Controller"
        }
        it("generate controller method based on spec operation method and url") {
            val controller = ControllerBuilder.build(Path("/pets", listOf(Operation(PathItem.HttpMethod.POST, null, null, null))), emptyList(), "apifi.gen", modelMapping())
            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].annotations[0].toString() shouldBe "@io.micronaut.http.annotation.Post(value = \"/pets\")"
            controllerClass.funSpecs[0].name shouldBe "post"
        }
        it("generate controller method with query, path and header params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, listOf(queryParam, pathParam, headerParam), null, null)

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.QueryValue limit: kotlin.Int",
                            "@io.micronaut.http.annotation.PathVariable petId: kotlin.Int",
                            "@io.micronaut.http.annotation.Header(value = \"x-header\") xHeader: kotlin.String",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
        }

        it("generate controller method with request and response") {
            val operation = Operation(PathItem.HttpMethod.POST, listOf(), "Pet", listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("@io.micronaut.http.annotation.Body body: models.Pet",
                            "httpRequest: io.micronaut.http.HttpRequest<kotlin.Any>")
            controllerClass.funSpecs[0].returnType.toString() shouldBe "io.micronaut.http.HttpResponse<PetResponse>"
        }

        it("generate controller method block with all blocks") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val operation = Operation(PathItem.HttpMethod.POST, listOf(queryParam, pathParam, headerParam), "Pet", listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            controllerClass.funSpecs[0].body.toString().trimIndent() shouldBe  "return basicauthorizer.authorize(httpRequest.headers.authorization){io.micronaut.http.HttpResponse.ok(service.post(limit, petId, body))}"
        }

        it("inject service as dependency") {
            val operation = Operation(PathItem.HttpMethod.POST, listOf(), "Pet", listOf("PetResponse"))

            val controller = ControllerBuilder.build(Path("/pets", listOf(operation)), emptyList(), "apifi.gen", modelMapping())

            val controllerClass = controller.members[0] as TypeSpec
            val serviceClass = controller.members[1] as TypeSpec
            controllerClass.name shouldBe "PetsController"
            serviceClass.name shouldBe "PetsService"

            controllerClass.propertySpecs[0].name shouldBe "service"
            controllerClass.propertySpecs[0].type.toString() shouldBe "apifi.gen.PetsService"
            controllerClass.propertySpecs[0].modifiers shouldContain KModifier.PRIVATE

            controllerClass.primaryConstructor!!.parameters.map { it.toString() } shouldContainExactlyInAnyOrder
                    listOf("service: apifi.gen.PetsService")
            controllerClass.primaryConstructor!!.toString() shouldBe "@javax.inject.Inject\n" +
                    "constructor(service: apifi.gen.PetsService) {\n" +
                    "  this.service = service\n" +
                    "}\n"
        }
    }


})

fun modelMapping() = listOf("Pet" to "models.Pet")
