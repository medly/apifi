package apifi.codegen

import apifi.parser.models.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.swagger.v3.oas.models.PathItem

class ServiceBuilderTest : DescribeSpec({

    describe("Service Builder") {
        it("should generate service class with operations") {
            val spec = Spec("pets", "api", listOf(Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "getOpName", null, null, null, SecurityDefinitionType.BASIC_AUTH),
                    Operation(PathItem.HttpMethod.POST, "postOpName", null, null, null, SecurityDefinitionType.BASIC_AUTH)
            ))), emptyList(), emptyList())
            val serviceClass = ServiceBuilder.build(spec, "Pets")
            serviceClass.name shouldBe "PetsService"
            serviceClass.funSpecs.size shouldBe 2
            serviceClass.toString().replace("\n","") shouldBe "interface PetsService {  fun getOpName()  fun postOpName()}"
        }

        it("should generate service class methods with methods including params") {
            val queryParam = Param("limit", "kotlin.Int", true, ParamType.Query)
            val pathParam = Param("petId", "kotlin.Int", true, ParamType.Path)
            val headerParam = Param("x-header", "kotlin.String", true, ParamType.Header)

            val spec = Spec("pets", "api", listOf(Path("/pets", listOf(
                    Operation(PathItem.HttpMethod.GET, "opName", listOf(queryParam, pathParam, headerParam), "Pet", listOf("PetResponse"), SecurityDefinitionType.BASIC_AUTH)
            ))), emptyList(), emptyList())
            val serviceClass = ServiceBuilder.build(spec, "Pets")

            serviceClass.funSpecs.size shouldBe 1
            serviceClass.funSpecs[0].toString().replace("\n","") shouldBe "abstract fun opName(  limit: kotlin.Int,  petId: kotlin.Int,  body: Pet): PetResponse"
        }
    }

})