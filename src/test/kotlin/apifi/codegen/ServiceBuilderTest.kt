package apifi.codegen

import apifi.parser.models.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ServiceBuilderTest : DescribeSpec({

    describe("Service Builder") {
        it("should generate service class with operations") {
            val serviceClass = ServiceBuilder.build(Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, null, null, null, SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, null, null, null, SecurityDefinitionType.BASIC_AUTH)
            )), "Pets")
            serviceClass.name shouldBe "PetsService"
            serviceClass.funSpecs.size shouldBe 2
            serviceClass.toString().replace("\n","") shouldBe "interface PetsService {  fun get()  fun post()}"
        }

        it("should generate service class methods with methods including params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)
            val serviceClass = ServiceBuilder.build(Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, listOf(queryParam, pathParam, headerParam), "Pet", listOf("PetResponse"), SecurityDefinitionType.BASIC_AUTH)
            )), "Pets")
            serviceClass.funSpecs.size shouldBe 1
            serviceClass.funSpecs[0].toString().replace("\n","") shouldBe "abstract fun get(  limit: kotlin.Int,  petId: kotlin.Int,  body: Pet): PetResponse"
        }
    }

})