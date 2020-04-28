package apifi.parser.models

data class Model(
        val name: String,
        val properties: List<Property>
)

data class Property(
        val name: String,
        val dataType: String,
        val nullable: Boolean
)
