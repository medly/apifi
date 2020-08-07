package apifi.codegen

import apifi.parser.models.Param
import apifi.parser.models.Response

fun testModelMapping() = mapOf("Pet" to "models.Pet", "PetResponse" to "models.PetResponse", "Error" to "models.Error")

fun emptyParams() = emptyList<Param>()
fun emptyResponses() = emptyList<Response>()
fun emptyTags() = emptyList<String>()