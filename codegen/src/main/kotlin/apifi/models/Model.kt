package apifi.models

data class Model(
    val name: String,
    val properties: List<Property>,
    val enumValues: List<String> = emptyList()
)

data class Property(
    val name: String,
    val dataType: String,
    val nullable: Boolean
)
